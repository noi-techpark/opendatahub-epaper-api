// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

/**
 * Controller class to create API for CRUD operations on Templates
 */
@RestController
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    TemplateRepository templateRepository;
    @Autowired
    DisplayRepository displayRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    private ImageUtil imageUtil;

    @Autowired
    private ResolutionRepository resolutionRepository;

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
        // download as image the display content
        byte[] image = fileImportStorageS3.download(template.getDisplayContent().getUuid());
        // InputStream is = new ByteArrayInputStream(image);
        // BufferedImage bImage = ImageIO.read(is);

        /*
         * if (withTextFields) { imageUtil.drawImageTextFields(bImage,
         * template.getDisplayContent().getImageFields(), null); }
         */
        // image = imageUtil.convertToByteArray(bImage, false, null); by neda
        // image = imageUtil.convertToByteArray(bImage, false,
        // template.getResolution());

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
        ResolutionDto resolutionDto = templateDto.getResolution();
        if (resolutionDto != null) {
            Resolution resolution = resolutionRepository.findByWidthAndHeightAndBitDepth(resolutionDto.getWidth(),
                    resolutionDto.getHeight(), resolutionDto.getBitDepth());
            if (resolution != null)
                template.setResolution(resolution);
            else {
                Resolution newResolution = new Resolution();
                newResolution.setWidth(resolutionDto.getWidth());
                newResolution.setHeight(resolutionDto.getHeight());
                newResolution.setBitDepth(resolutionDto.getBitDepth());
                template.setResolution(newResolution);
            }
        }
        template.setFooter(templateDto.getFooter());
        template.setHeader(templateDto.getHeader());
        template.setMultipleRoom(templateDto.getMultipleRoom());
        template.setRoomData(templateDto.getRoomData());

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
        ResolutionDto resolutionDto = templateDto.getResolution();
        if (resolutionDto != null) {
            Resolution resolution = resolutionRepository.findByWidthAndHeightAndBitDepth(resolutionDto.getWidth(),
                    resolutionDto.getHeight(), resolutionDto.getBitDepth());
            if (resolution != null)
                template.setResolution(resolution);
            else {
                Resolution newResolution = new Resolution();
                newResolution.setWidth(resolutionDto.getWidth());
                newResolution.setHeight(resolutionDto.getHeight());
                newResolution.setBitDepth(resolutionDto.getBitDepth());
                template.setResolution(newResolution);
            }
        }

        template.setName(templateDto.getName());
        template.setDescription(templateDto.getDescription());
        template.setFooter(templateDto.getFooter());
        template.setHeader(templateDto.getHeader());
        template.setMultipleRoom(templateDto.getMultipleRoom());
        template.setRoomData(templateDto.getRoomData());

        template.setLastUpdate(new Date());

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
        template.getDisplayContent().setImageFields(displayContent.getImageFields());// set upload
        // template.getDisplayContent().setImageBase64(displayContent.getImageBase64());
        template.getDisplayContent()
                .setImageBase64(imageUtil.drawImageTextFields(template.getDisplayContent().getImageFields(),
                        template.getResolution().getWidth(), template.getResolution().getHeight(), template)); // quaa

        if (template.getDisplayContent().getImageBase64() != null) {
            // InputStream in = new ByteArrayInputStream(image.getBytes());
            // BufferedImage bImageFromConvert = ImageIO.read(in);

            BufferedImage bImageFromConvert = null;
            byte[] imageBytes = Base64.getDecoder().decode(template.getDisplayContent().getImageBase64());
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            bImageFromConvert = ImageIO.read(bis);

            String fileKey = template.getDisplayContent().getUuid();
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);
        }
        template.setLastUpdate(new Date());
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

        List<Display> displays = displayRepository.findAll();
        for (Display display : displays) {
            if (display.getTemplate().equals(template)) {
                System.out.println("helloo");
                logger.debug("Template is assigned to a display, cannot be deleted");
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        }

        templateRepository.delete(template);
        logger.debug("Deleted template with uuid:" + uuid);
        return new ResponseEntity(HttpStatus.OK);

    }

    public ResolutionRepository getResolutionRepository() {
        return resolutionRepository;
    }

    public void setResolutionRepository(ResolutionRepository resolutionRepository) {
        this.resolutionRepository = resolutionRepository;
    }
}
