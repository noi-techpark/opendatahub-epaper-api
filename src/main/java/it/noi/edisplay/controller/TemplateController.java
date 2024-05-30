// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.ResolutionRepository;
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
    private ResolutionRepository resolutionRepository;

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
            int roomAmount = template.getMaxRooms();
            int padding = template.getDisplayContent().getPadding();
            int roomSectionHeight = (template.getResolution().getHeight() - (padding * 2)) / roomAmount;

            for (int roomIndex = 0; roomIndex <= roomAmount; roomIndex++) {
                imageUtil.drawImageTextFields(bImage, template.getDisplayContent().getImageFields(), null,
                        roomIndex, roomSectionHeight, padding);
            }
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

        ResolutionDto resolutionDto = templateDto.getResolution();
        if (resolutionDto != null) {
            Resolution resolution = resolutionRepository.findByWidthAndHeightAndBitDepth(resolutionDto.getWidth(),
                    resolutionDto.getHeight(), resolutionDto.getBitDepth());
            template.setResolution(resolution);
        }

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
        template.getDisplayContent().setPadding(displayContent.getPadding());

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
