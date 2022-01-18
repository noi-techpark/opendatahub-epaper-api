package it.noi.edisplay.controller;

import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.utils.ImageUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

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
            @RequestParam(value = "image", required = false) MultipartFile image)
            throws JsonMappingException, JsonProcessingException {
        TemplateDto templateDto = new ObjectMapper().readValue(templateJson, TemplateDto.class);
        Template template = modelMapper.map(templateDto, Template.class);

        if (templateDto.getUuid() != null) {
            Template existingTemplate = templateRepository.findByUuid(templateDto.getUuid());
            if (existingTemplate == null) {
                logger.debug("Update template with uuid:" + templateDto.getUuid() + " failed.");
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }

            if (image == null && existingTemplate.getDisplayContent() != null) {
                template.getDisplayContent().setImageHash(existingTemplate.getDisplayContent().getImageHash());
                template.getDisplayContent().setImageUrl(existingTemplate.getDisplayContent().getImageUrl());
            } else {
                // upload image
            }
        }

        // Model mapper creates objects for these randomly so they have to be set to null
        template.getDisplayContent().setDisplay(null);
        template.getDisplayContent().setScheduledContent(null);

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
