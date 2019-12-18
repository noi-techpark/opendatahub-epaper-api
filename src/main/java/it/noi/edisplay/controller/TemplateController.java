package it.noi.edisplay.controller;

import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.TemplateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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

    @RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable("uuid") String uuid) {
        Template template = templateRepository.findByUuid(uuid);

        if (template == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(modelMapper.map(template, TemplateDto.class), HttpStatus.OK);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity getAllTemplates() {
        return new ResponseEntity<>(modelMapper.map(templateRepository.findAll(), ArrayList.class), HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity createTemplate(@RequestBody TemplateDto templateDto) {
        Template template = modelMapper.map(templateDto, Template.class);
        return new ResponseEntity<>(modelMapper.map(templateRepository.saveAndFlush(template), TemplateDto.class), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
    public ResponseEntity deleteTemplate(@PathVariable("uuid") String uuid) {
        Template template = templateRepository.findByUuid(uuid);

        if (template == null)
            return new ResponseEntity(HttpStatus.BAD_REQUEST);

        try {
            templateRepository.delete(template);
        } catch (EmptyResultDataAccessException ex) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);

    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity updateTemplate(@RequestBody TemplateDto templateDto) {
        Template template = templateRepository.findByUuid(templateDto.getUuid());
        if (template == null)
            return new ResponseEntity(HttpStatus.BAD_REQUEST);

        template.setName(templateDto.getName());
        template.setImage(templateDto.getImage());
        templateRepository.save(template);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

}

