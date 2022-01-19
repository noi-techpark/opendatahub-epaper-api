package it.noi.edisplay.controller;

import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.UUID;

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

    @RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable("uuid") String uuid) {
        Template template = templateRepository.findByUuid(uuid);

        if (template == null) {
            logger.debug("Template with uuid: " + uuid + " not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.debug("Get display with uuid: " + uuid);
        return new ResponseEntity<>(modelMapper.map(template, TemplateDto.class), HttpStatus.OK);
    }

    @RequestMapping(value = "/getImage/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getTemplateImage(@PathVariable("uuid") String uuid,
            @RequestParam(value = "convertToBMP", required = false) Boolean convertToBMP) throws IOException {
        if (convertToBMP == null) {
            convertToBMP = false;
        }

        Template template = templateRepository.findByUuid(uuid);

        if (template == null) {
            logger.debug("Template with uuid: " + uuid + " not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        byte[] image = fileImportStorageS3.download(template.getDisplayContent().getUuid());

        BufferedImage bImage = imageUtil.setImageFields(image, template.getDisplayContent().getImageFields(), null);
        image = imageUtil.convertToByteArray(bImage, convertToBMP);

        logger.debug("Get template image with uuid: " + uuid);
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity getAllTemplates() {
        List<Template> list = templateRepository.findAll();
        ArrayList<TemplateDto> dtoList = new ArrayList<>();
        for (Template template : list)
            dtoList.add(modelMapper.map(template, TemplateDto.class));
        logger.debug("All templates requested");
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "multipart/form-data")
    public ResponseEntity createOrUpdateTemplate(@RequestParam("template") String templateJson,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        TemplateDto templateDto = new ObjectMapper().readValue(templateJson, TemplateDto.class);
        Template template = modelMapper.map(templateDto, Template.class);

        if (template.getUuid() != null) {
            Template existingTemplate = templateRepository.findByUuid(template.getUuid());
            if (existingTemplate == null) {
                logger.debug("Update template with uuid:" + template.getUuid() + " failed.");
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }

            existingTemplate.setName(template.getName());
            existingTemplate.setDescription(template.getDescription());
            existingTemplate.getDisplayContent().setImageFields(template.getDisplayContent().getImageFields());

            template = existingTemplate;
        } else {
            template.getDisplayContent().setUuid(UUID.randomUUID().toString());
        }

        // Model mapper creates objects for these randomly so they have to be set to null
        template.getDisplayContent().setDisplay(null);
        template.getDisplayContent().setScheduledContent(null);
        template.getDisplayContent().setTemplate(template);
        
        if (image != null) {
            InputStream in = new ByteArrayInputStream(image.getBytes());
            BufferedImage bImageFromConvert = ImageIO.read(in);
            String fileKey = template.getDisplayContent().getUuid();
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);
        }
        
        Template savedTemplate = templateRepository.saveAndFlush(template);

        if (templateDto.getUuid() != null) {
            logger.debug("Updated template with uuid:" + template.getUuid());
            return new ResponseEntity(HttpStatus.ACCEPTED);
        }

        logger.debug("Template with uuid:" + savedTemplate.getUuid() + " created.");
        return new ResponseEntity<>(modelMapper.map(savedTemplate, TemplateDto.class), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
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
