package it.noi.edisplay.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

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
import it.noi.edisplay.model.ImageField;
import it.noi.edisplay.model.ImageFieldType;
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
            throws IOException, NoSuchAlgorithmException {
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

        Map<ImageFieldType, String> fieldValues = null;
        if (withTextFields) {
            fieldValues = display.getTextFieldValues();
            imageUtil.setImageFields(bImage, display.getDisplayContent().getImageFields(), fieldValues);
        }
        image = imageUtil.convertToByteArray(bImage, convertToBMP, display.getResolution());

        // Set MD5 hash for Display if the image is in native format
        if (convertToBMP) {
            if (fieldValues != null) {
                // Set current field values for later MD5 validation
                for (ImageField field : display.getDisplayContent().getImageFields()) {
                    if (field.getFieldType() != ImageFieldType.CUSTOM_TEXT) {
                        field.setCurrentFieldValue(fieldValues.get(field.getFieldType()));
                    }
                }
            }

            display.setImageHash(imageUtil.convertToMD5Hash(image));
            displayRepository.saveAndFlush(display);
        }

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
        
        // Display content has changed, so the current image hash is no longer valid
        display.setImageHash(null);

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
            @PathVariable("displayUuid") String displayUuid, String templateUuid) {
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
        
        // Display content has changed, so the current image hash is no longer valid
        display.setImageHash(null);

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
