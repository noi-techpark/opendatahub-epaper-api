// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.services.TemplateService;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Controller class to create API for CRUD operations on Templates
 */
@RestController
@RequestMapping("/template")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @GetMapping("/get/{uuid}")
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable("uuid") String uuid) {
        TemplateDto templateDto = templateService.getTemplateByUuid(uuid);
        if (templateDto == null) {
            logger.debug("Template with uuid: {} not found.", uuid);
            return ResponseEntity.badRequest().build();
        }
        logger.debug("Get template with uuid: ", uuid);
        return ResponseEntity.ok(templateDto);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TemplateDto>> getAllTemplates() {
        List<TemplateDto> templateDtos = templateService.getAllTemplates();
        logger.debug("All templates requested");
        return ResponseEntity.ok(templateDtos);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTemplate(@RequestBody TemplateDto templateDto) {
        try {
            TemplateDto created = templateService.createTemplate(templateDto);
            logger.debug("Template created: {}", created.getUuid());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.warn("Template creation failed: {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().body(e.getMessage());
        }
    }

    @PutMapping(value = "/update", consumes = "application/json")
    public ResponseEntity<?> updateTemplate(@RequestBody TemplateDto templateDto) {
        try {
            templateService.updateTemplate(templateDto);
            logger.debug("Updated to template with uuid: {}", templateDto.getUuid());
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Template update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/set-image/{templateUuid}", consumes = "multipart/form-data")
    public ResponseEntity<?> setTemplateContent(
            @PathVariable("templateUuid") String templateUuid,
            @RequestParam("displayContentDtoJson") String displayContentDtoJson,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
                try {
                    DisplayContentDto result = templateService.setTemplateImage(templateUuid, displayContentDtoJson, image);
                    logger.debug("Set image for Template uuid: {}", templateUuid);
                    return ResponseEntity.status(HttpStatus.CREATED).body(result);
                } catch (IllegalArgumentException e) {
                    logger.warn("Set image failed: {}", e.getMessage());
                    return ResponseEntity.badRequest().body(e.getMessage());
                }
    }

    @DeleteMapping("/delete/{uuid}")
    public ResponseEntity<?> deleteTemplate(@PathVariable("uuid") String uuid) {
        try {
            templateService.deleteTemplate(uuid);
            logger.debug("Deleted template with uuid: {}", uuid);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Delete failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/get-image/{uuid}")
    public ResponseEntity<byte[]> getTemplateImage(
        @PathVariable("uuid") String uuid, 
        @RequestParam(name = "withTextFields", defaultValue = "false") boolean withTextFields) throws IOException {
            byte[] image = templateService.getTemplateImage(uuid, withTextFields);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(image);
    }
    
}
