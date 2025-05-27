// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.noi.edisplay.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

// import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.noi.edisplay.components.NOIDataLoader;
import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.ScheduledContentDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.ScheduledContent;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.ScheduledContentRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

@Service
public class ScheduledContentService {
    
    private final ScheduledContentRepository scheduledContentRepository;
    private final DisplayRepository displayRepository;
    private final TemplateRepository templateRepository;
    private final NOIDataLoader noiDataLoader;
    private final FileImportStorageS3 fileImportStorageS3;
    private final ImageUtil imageUtil;
    private final ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(ScheduledContentService.class);

    public ScheduledContentService(
        ScheduledContentRepository scheduledContentRepository,
        DisplayRepository displayRepository,
        TemplateRepository templateRepository,
        NOIDataLoader noiDataLoader,
        FileImportStorageS3 fileImportStorageS3,
        ImageUtil imageUtil,
        ModelMapper modelMapper
    ) {
        this.scheduledContentRepository = scheduledContentRepository;
        this.displayRepository = displayRepository;
        this.templateRepository = templateRepository;
        this.noiDataLoader = noiDataLoader;
        this.fileImportStorageS3 = fileImportStorageS3;
        this.imageUtil = imageUtil;
        this.modelMapper = modelMapper;
    }

    public ResponseEntity<ScheduledContentDto> getScheduledContent(String uuid) {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(uuid);

        if (scheduledContent == null) {
            logger.debug("Scheduled content with uuid: {} not found!", uuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        logger.debug("Get scheduled content wit h uuid: {}", uuid);
        return new ResponseEntity<>(modelMapper.map(scheduledContent, ScheduledContentDto.class), HttpStatus.OK);
    }

    public ResponseEntity<byte[]> getScheduledImage(String uuid, boolean withTextFields) throws IOException {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(uuid);
        
        if (scheduledContent == null || scheduledContent.getDisplayContent() == null) {
            logger.debug("Scheduled content with uuid: {} not found or has got no image!", uuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        byte[] image = fileImportStorageS3.download(scheduledContent.getDisplay().getUuid());
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bImage = ImageIO.read(is);

        if (withTextFields) {
            int roomAmount = scheduledContent.getDisplayContent().getTemplate().getMaxRooms();
            int padding = scheduledContent.getDisplayContent().getPadding();
            int roomSectionHeight = (scheduledContent.getDisplayContent().getTemplate().getResolution().getHeight() - (padding * 2)) / roomAmount;
            
            for (int roomIndex = 0; roomIndex < roomAmount; roomIndex++) {
                imageUtil.drawImageTextFields(
                    bImage,
                    scheduledContent.getDisplayContent().getImageFields(),
                    null,
                    roomIndex,
                    roomSectionHeight,
                    padding
                );
            }
        }

        image = imageUtil.convertToByteArray(bImage, false, null);
        logger.debug("Get scheduled content image with uuid: {}", uuid);
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    public ResponseEntity<List<ScheduledContentDto>> getAllScheduledContents(String displayUuid) {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid: {} cannot be found!", displayUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<ScheduledContentDto> scheduledContentDtos = noiDataLoader.getAllDisplayEvents(display);

        logger.debug("{} scheduled contents requested!", scheduledContentDtos.size());
        return new ResponseEntity<>(scheduledContentDtos, HttpStatus.OK);
    }

    public ResponseEntity<ScheduledContentDto> createScheduledContent(ScheduledContentDto scheduledContentDto) {
        ScheduledContent scheduledContent = modelMapper.map(scheduledContentDto, ScheduledContent.class);
        scheduledContent.setDisplay(displayRepository.findByUuid(scheduledContentDto.getDisplayUuid()));
        scheduledContent = scheduledContentRepository.saveAndFlush(scheduledContent);

        logger.debug("Scheduled content created with uuid: {}!", scheduledContent.getUuid());
        return new ResponseEntity<>(modelMapper.map(scheduledContent, ScheduledContentDto.class), HttpStatus.CREATED);
    }

    public ResponseEntity<Void> deleteScheduledContent(String uuid) {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(uuid);
        if (scheduledContent == null) {
            logger.debug("Scheduled content with uuid: {} not found!", uuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        scheduledContentRepository.delete(scheduledContent);
        logger.debug("scheduled content with uuid: {} deleted successfully!", uuid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Void> updateSchdeduledContent(ScheduledContentDto scheduledContentDto) {
        ScheduledContent existingScheduledContent = findExistingScheduledContent(scheduledContentDto);

        if (existingScheduledContent == null) {
            ScheduledContent newScheduledContent = modelMapper.map(scheduledContentDto, ScheduledContent.class);
            scheduledContentRepository.saveAndFlush(newScheduledContent);
            logger.debug("Created a new scheduled content with uuid {}", newScheduledContent.getUuid());
            return new ResponseEntity<>(HttpStatus.CREATED);
        }

        // update existing scheduled content
        existingScheduledContent.setDisabled(scheduledContentDto.getDisabled());
        existingScheduledContent.setStartDate(scheduledContentDto.getStartDate());
        existingScheduledContent.setEndDate(scheduledContentDto.getEndDate());
        existingScheduledContent.setEventDescription(scheduledContentDto.getEventDescription());
        existingScheduledContent.setSpaceDesc(scheduledContentDto.getSpaceDesc());
        scheduledContentRepository.saveAndFlush(existingScheduledContent);

        logger.debug("updated scheduled content wit uuid: {}", existingScheduledContent.getUuid());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    private ScheduledContent findExistingScheduledContent(ScheduledContentDto scheduledContentDto) {
        if (scheduledContentDto.getUuid() != null) {
            return scheduledContentRepository.findByUuid(scheduledContentDto.getUuid());
        } 
        Display display = displayRepository.findByUuid(scheduledContentDto.getDisplayUuid());
        return scheduledContentRepository.findByDisplayIdAndEventId(display.getId(), scheduledContentDto.getEventId());
    }

    public ResponseEntity<DisplayContentDto> setDisplayContent(String scheduledContentUuid, String displayContentDtoJson, MultipartFile image) throws IOException {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(scheduledContentUuid);

        if (scheduledContent == null) {
            logger.debug("Scheduled content ith uuid: {} cannot be found!", scheduledContentUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContentDto displayContentDto = new ObjectMapper().readValue(displayContentDtoJson, DisplayContentDto.class);
        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean displayContentExists = scheduledContent.getDisplayContent() != null;

        if (!displayContentExists) {
            scheduledContent.setDisplayContent(new DisplayContent());
            scheduledContent.getDisplayContent().setScheduledContent(scheduledContent);
        }

        scheduledContent.getDisplayContent().setImageFields(displayContent.getImageFields());
        scheduledContent.getDisplayContent().setPadding(displayContent.getPadding());

        if (image != null) {
            InputStream is = new ByteArrayInputStream(image.getBytes());
            BufferedImage bImage = ImageIO.read(is);
            String fileKey = scheduledContent.getDisplayContent().getUuid();
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImage), fileKey);
        }

        // Display content has changed. current image no longer valid
        scheduledContent.getDisplayContent().setImageHash(null);

        ScheduledContent savedScheduledContent = scheduledContentRepository.saveAndFlush(scheduledContent);

        if (displayContentExists) {
            logger.debug("Updated image for scheduled content with uuid: {}", scheduledContentUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created image for scheduled content with uuid: {}", scheduledContentUuid);
        return new ResponseEntity<>(modelMapper.map(savedScheduledContent.getDisplayContent(), DisplayContentDto.class), HttpStatus.CREATED);
    }

    public ResponseEntity<DisplayContentDto> setScheduledContentByTemplate(String scheduledContentUuid, String templateUuid, DisplayContentDto displayContentDto) {
        ScheduledContent scheduledContent = scheduledContentRepository.findByUuid(scheduledContentUuid);

        if (scheduledContent == null) {
            logger.debug("Schedule content with uuid: {} cannot be found!", scheduledContentUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Template template = templateRepository.findByUuid(templateUuid);

        if (template == null) {
            logger.debug("Template with uuid: {} cannot be found!", templateUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean displayContentExists = scheduledContent.getDisplayContent() != null;

        if (!displayContentExists) {
            scheduledContent.setDisplayContent(new DisplayContent());
            scheduledContent.getDisplayContent().setScheduledContent(scheduledContent);
        }

        if (template.getDisplayContent() != null) {
            // copy background image from template
            fileImportStorageS3.copy(template.getDisplayContent().getUuid(), scheduledContent.getDisplay().getUuid());
        }

        scheduledContent.getDisplayContent().setImageFields(displayContent.getImageFields());
        scheduledContent.getDisplayContent().setPadding(displayContent.getPadding());

        // Display content has chnaged, image hash is not valid any longer
        scheduledContent.getDisplayContent().setImageHash(null);

        ScheduledContent savedScheduledContent = scheduledContentRepository.saveAndFlush(scheduledContent);

        if (displayContentExists) {
            logger.debug("Updated imge for scheduled content uuid: {}", scheduledContentUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("created image for scheduled content uuid: {}", scheduledContentUuid);
        return new ResponseEntity<>(modelMapper.map(savedScheduledContent, DisplayContentDto.class), HttpStatus.CREATED);
    }

}
