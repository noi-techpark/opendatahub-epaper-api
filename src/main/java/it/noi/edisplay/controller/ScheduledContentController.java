// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import java.io.IOException;
import java.util.List;

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

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.ScheduledContentDto;
import it.noi.edisplay.services.ScheduledContentService;

@RestController
@RequestMapping("/ScheduledContent")
public class ScheduledContentController {

    private ScheduledContentService scheduledContentService;

    ScheduledContentController(ScheduledContentService scheduledContentService) {
        this.scheduledContentService = scheduledContentService;
    }

    @GetMapping("/get/{uuid}")
    public ResponseEntity<ScheduledContentDto> getScheduledContent(@PathVariable("uuid") String uuid) {
        return scheduledContentService.getScheduledContent(uuid);
    }
    
    @GetMapping("/get-image/{uuid}")
    public ResponseEntity<byte[]> getScheduledImage(@PathVariable("uuid") String uuid, boolean withTextFields)
            throws IOException {
        return scheduledContentService.getScheduledImage(uuid, withTextFields);

    }
    
    @GetMapping("/all")
    public ResponseEntity<List<ScheduledContentDto>> getAllScheduledContents(@RequestParam String displayUuid) {
        return scheduledContentService.getAllScheduledContents(displayUuid);
    }
    
    @PostMapping("/create")
    public ResponseEntity<ScheduledContentDto> createScheduledContent(@RequestBody ScheduledContentDto scheduledContentDto) {
        return scheduledContentService.createScheduledContent(scheduledContentDto);
    }

    @DeleteMapping("/delete/{uuid}")
    public ResponseEntity<Void> deleteScheduledContent(@PathVariable("uuid") String uuid) {
        return scheduledContentService.deleteScheduledContent(uuid);
    }
    
    @PutMapping("/update")
    public ResponseEntity<Void> updateScheduledContent(@RequestBody ScheduledContentDto scheduledContentDto) {
        return scheduledContentService.updateSchdeduledContent(scheduledContentDto);
    }

    @PostMapping(value = "/set-new-image/{scheduledContentUuid}", consumes = "multipart/form-data")
    public ResponseEntity<DisplayContentDto> setDisplayContent(
            @PathVariable("scheduledContentUuid") String scheduledContentUuid,
            @RequestParam("displayContentDtoJson") String displayContentDtoJson,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        return scheduledContentService.setDisplayContent(scheduledContentUuid, displayContentDtoJson, image);
    }

    @PostMapping("/set-template-image/{scheduledContentUuid}")
    public ResponseEntity<DisplayContentDto> setScheduledContentByTemplate(
            @PathVariable("scheduledContentUuid") String scheduledContentUuid, String templateUuid,
            @RequestBody DisplayContentDto displayContentDto) {
        return scheduledContentService.setScheduledContentByTemplate(scheduledContentUuid, templateUuid, displayContentDto);
    }
}
