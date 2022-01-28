package it.noi.edisplay.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;

import javax.imageio.ImageIO;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.ImageFieldType;
import it.noi.edisplay.model.ScheduledContent;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

/**
 * Controller class to create API for CRUD operations on Display Content
 */
@RestController
@RequestMapping("/display-content")
public class DisplayContentController {
    @Autowired
    private DisplayRepository displayRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileImportStorageS3 fileImportStorageS3;

    @Autowired
    private ImageUtil imageUtil;

    private Logger logger = LoggerFactory.getLogger(DisplayContentController.class);

    @GetMapping(value = "/get-image/{displayUuid}")
    public ResponseEntity<byte[]> getDisplayContent(@PathVariable("displayUuid") String displayUuid,
            @RequestParam(value = "convertToBMP", required = false) boolean convertToBMP, boolean withTextFields)
            throws IOException {
        Display display = displayRepository.findByUuid(displayUuid);
        
        if (display == null) {
            logger.debug("Display with uuid " + displayUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (display.getDisplayContent() == null) {
            logger.debug("Display with uuid " + displayUuid + " has no image.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] image = fileImportStorageS3.download(display.getDisplayContent().getUuid());
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bImage = ImageIO.read(is);

        if (withTextFields) {
            EnumMap<ImageFieldType, String> fieldValues = new EnumMap<>(ImageFieldType.class);
            
            // Location
            if (display.getLocation() != null) {
                fieldValues.put(ImageFieldType.LOCATION_NAME, display.getLocation().getName());
            } else {
                fieldValues.put(ImageFieldType.LOCATION_NAME, "Location not specified");
            }

            List<ScheduledContent> events = display.getScheduledContent();
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 

            // Current Event
            Date currentDate = new Date();
            ScheduledContent currentEvent = events.stream()
                    .filter(item -> item.getStartDate().before(currentDate) && item.getEndDate().after(currentDate))
                    .findFirst().orElse(null);
            if (currentEvent != null) {
                fieldValues.put(ImageFieldType.EVENT_DESCRIPTION, currentEvent.getEventDescription());
                fieldValues.put(ImageFieldType.EVENT_START_DATE, f.format(currentEvent.getStartDate()));
                fieldValues.put(ImageFieldType.EVENT_END_DATE, f.format(currentEvent.getEndDate()));
            } else {
                fieldValues.put(ImageFieldType.EVENT_DESCRIPTION, "No current event");
                fieldValues.put(ImageFieldType.EVENT_START_DATE, "");
                fieldValues.put(ImageFieldType.EVENT_END_DATE, "");
            }

            // Upcoming event
            Collections.sort(events); // Sort events by start date
            if (!events.isEmpty()) {
                ScheduledContent upcomingEvent = events.get(0);
                fieldValues.put(ImageFieldType.UPCOMING_EVENT_DESCRIPTION, upcomingEvent.getEventDescription());
                fieldValues.put(ImageFieldType.UPCOMING_EVENT_START_DATE, f.format(upcomingEvent.getStartDate()));
                fieldValues.put(ImageFieldType.UPCOMING_EVENT_END_DATE, f.format(upcomingEvent.getEndDate()));
            } else {
                fieldValues.put(ImageFieldType.UPCOMING_EVENT_DESCRIPTION, "No upcoming events");
                fieldValues.put(ImageFieldType.UPCOMING_EVENT_START_DATE, "");
                fieldValues.put(ImageFieldType.UPCOMING_EVENT_END_DATE, "");
            }

            imageUtil.setImageFields(bImage, display.getDisplayContent().getImageFields(), fieldValues);
        }
        image = imageUtil.convertToByteArray(bImage, convertToBMP);

        logger.debug("Get display image with uuid: " + displayUuid);
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    @PostMapping(value = "/set/{displayUuid}", consumes = "multipart/form-data")
    public ResponseEntity<DisplayContentDto> setDisplayContent(@PathVariable("displayUuid") String displayUuid,
            String templateUuid, @RequestParam("displayContent") String displayContentJson,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid " + displayUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContentDto displayContentDto = new ObjectMapper().readValue(displayContentJson, DisplayContentDto.class);
        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean displayContentExists = display.getDisplayContent() != null;

        if (!displayContentExists) {
            display.setDisplayContent(new DisplayContent());
            display.getDisplayContent().setUuid(UUID.randomUUID().toString());
            display.getDisplayContent().setDisplay(display);
        }
        display.getDisplayContent().setImageFields(displayContent.getImageFields());

        if (image != null) {
            InputStream in = new ByteArrayInputStream(image.getBytes());
            BufferedImage bImageFromConvert = ImageIO.read(in);
            String fileKey = display.getDisplayContent().getUuid();
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);
        } else if (templateUuid != null) {
            Template template = templateRepository.findByUuid(templateUuid);

            if (template == null) {
                logger.debug("Template with uuid " + displayUuid + " was not found.");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (template.getDisplayContent() != null) {
                // Copy background image from template
                fileImportStorageS3.copy(template.getDisplayContent().getUuid(), display.getDisplayContent().getUuid());
            }
        }

        Display savedDisplay = displayRepository.saveAndFlush(display);

        if (displayContentExists) {
            logger.debug("Updated display content for Display uuid:" + displayUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created display content for Display uuid:" + displayUuid);
        return new ResponseEntity<>(modelMapper.map(savedDisplay.getDisplayContent(), DisplayContentDto.class),
                HttpStatus.CREATED);
    }

    @PostMapping(value = "/set-by-template/{displayUuid}")
    public ResponseEntity<DisplayContentDto> setDisplayContentByTemplate(
            @PathVariable("displayUuid") String displayUuid, String templateUuid) throws IOException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid " + displayUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Template template = templateRepository.findByUuid(templateUuid);

        if (template == null) {
            logger.debug("Template with uuid " + displayUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        boolean displayContentExists = display.getDisplayContent() != null;

        if (template.getDisplayContent() != null) {
            if (!displayContentExists) {
                display.setDisplayContent(new DisplayContent());
                display.getDisplayContent().setUuid(UUID.randomUUID().toString());
                display.getDisplayContent().setDisplay(display);
            }
            display.getDisplayContent().setImageFields(template.getDisplayContent().getImageFields());

            // Copy background image from template
            fileImportStorageS3.copy(template.getDisplayContent().getUuid(), display.getDisplayContent().getUuid());
        }

        Display savedDisplay = displayRepository.saveAndFlush(display);

        if (displayContentExists) {
            logger.debug("Updated display content for Display uuid:" + displayUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created display content for Display uuid:" + displayUuid);
        return new ResponseEntity<>(modelMapper.map(savedDisplay.getDisplayContent(), DisplayContentDto.class),
                HttpStatus.CREATED);
    }
}
