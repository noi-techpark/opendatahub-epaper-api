package it.noi.edisplay.controller;

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Controller class to create API for CRUD operations on Templates
 */
@RestController
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    TemplateRepository templateRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private ImageUtil imageUtil;

    @Autowired
    private FileImportStorageS3 fileImportStorageS3;

    Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @GetMapping(value = "/get/{uuid}")
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable("uuid") String uuid) {
        Template template = templateRepository.findByUuid(uuid);

        if (template == null) {
            logger.debug("Template with uuid: " + uuid + " not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.debug("Get display with uuid: " + uuid);
        return new ResponseEntity<>(modelMapper.map(template, TemplateDto.class), HttpStatus.OK);
    }

    @GetMapping(value = "/get-image/{uuid}")
    public ResponseEntity<byte[]> getTemplateImage(@PathVariable("uuid") String uuid, boolean withTextFields)
            throws IOException {
        Template template = templateRepository.findByUuid(uuid);

        if (template == null) {
            logger.debug("Template with uuid: " + uuid + " not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (template.getDisplayContent() == null) {
            logger.debug("Template with uuid: " + uuid + " has no image.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] image = fileImportStorageS3.download(template.getDisplayContent().getUuid());
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bImage = ImageIO.read(is);

        if (withTextFields) {
            imageUtil.setImageFields(bImage, template.getDisplayContent().getImageFields(), null);
        }
        image = imageUtil.convertToByteArray(bImage, false, null);

        logger.debug("Get template image with uuid: " + uuid);
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<ArrayList<TemplateDto>> getAllTemplates() {
        List<Template> list = templateRepository.findAll();
        ArrayList<TemplateDto> dtoList = new ArrayList<>();
        for (Template template : list)
            dtoList.add(modelMapper.map(template, TemplateDto.class));
        logger.debug("All templates requested");
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @PostMapping(value = "/create")
    public ResponseEntity<Object> createTemplate(@RequestBody TemplateDto templateDto) {
        Template template = modelMapper.map(templateDto, Template.class);
        try {
            template = templateRepository.saveAndFlush(template);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause != null)
                return new ResponseEntity<>(rootCause.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
            else
                throw (e);
        }
        logger.debug("Template with uuid:" + template.getUuid() + " created.");
        return new ResponseEntity<>(modelMapper.map(template, TemplateDto.class), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update", consumes = "application/json")
    public ResponseEntity<Object> updateTemplate(@RequestBody TemplateDto templateDto) {
        Template template = templateRepository.findByUuid(templateDto.getUuid());

        if (template == null) {
            logger.debug("Update template with uuid:" + templateDto.getUuid() + " failed.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        template.setName(templateDto.getName());
        template.setDescription(templateDto.getDescription());
        try {
            templateRepository.saveAndFlush(template);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause != null)
                return new ResponseEntity<>(rootCause.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
            else
                throw (e);
        }

        logger.debug("Updated template with uuid:" + template.getUuid());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/set-image/{templateUuid}", consumes = "multipart/form-data")
    public ResponseEntity<DisplayContentDto> setTemplateContent(@PathVariable("templateUuid") String templateUuid,
            @RequestParam("displayContentDtoJson") String displayContentDtoJson,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        Template template = templateRepository.findByUuid(templateUuid);

        if (template == null) {
            logger.debug("Template with uuid " + templateUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContentDto displayContentDto = new ObjectMapper().readValue(displayContentDtoJson,
                DisplayContentDto.class);
        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean templateContentExists = template.getDisplayContent() != null;

        if (!templateContentExists) {
            template.setDisplayContent(new DisplayContent());
            template.getDisplayContent().setTemplate(template);
        }
        template.getDisplayContent().setImageFields(displayContent.getImageFields());

        if (image != null) {
            InputStream in = new ByteArrayInputStream(image.getBytes());
            BufferedImage bImageFromConvert = ImageIO.read(in);
            String fileKey = template.getDisplayContent().getUuid();
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);
        }

        Template savedTemplate = templateRepository.saveAndFlush(template);

        if (templateContentExists) {
            logger.debug("Updated image for Template uuid:" + templateUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created image for Template uuid:" + templateUuid);
        return new ResponseEntity<>(modelMapper.map(savedTemplate.getDisplayContent(), DisplayContentDto.class),
                HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/delete/{uuid}")
    public ResponseEntity deleteTemplate(@PathVariable("uuid") String uuid) {
        Template template = templateRepository.findByUuid(uuid);

        if (template == null) {
            logger.debug("Delete template with uuid:" + uuid + " failed.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        templateRepository.delete(template);
        logger.debug("Deleted template with uuid:" + uuid);
        return new ResponseEntity(HttpStatus.OK);

    }
}
