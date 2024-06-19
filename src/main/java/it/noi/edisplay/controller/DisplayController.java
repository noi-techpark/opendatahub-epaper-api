// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.DisplayStateDto;
import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.dto.TemplateDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.ImageField;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.DisplayContentRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.repositories.ScheduledContentRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

/**
 * Controller class to create API for CRUD operations on Displays
 */
@RestController
@RequestMapping("/display")
public class DisplayController {

    @Value("${event.advance}")
    private int eventAdvance;

    @Autowired
    private DisplayRepository displayRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ResolutionRepository resolutionRepository;

    @Autowired
    private DisplayContentRepository displayContentRepository;

    @Autowired
    ScheduledContentRepository scheduledContentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileImportStorageS3 fileImportStorageS3;

    @Autowired
    private ImageUtil imageUtil;

    private Logger logger = LoggerFactory.getLogger(DisplayController.class);

    @RequestMapping(value = "/get/{displayUuid}", method = RequestMethod.GET)
    public ResponseEntity<DisplayDto> getDisplay(@PathVariable("displayUuid") String uuid) {
        Display display = displayRepository.findByUuid(uuid);
        if (display == null) {
            logger.debug("Display with uuid: " + uuid + " not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.debug("Get display with uuid: " + uuid);
        return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.OK);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity getAllDisplays() {
        List<Display> list = displayRepository.findAll();
        ArrayList<DisplayDto> dtoList = new ArrayList<>();
        for (Display display : list) {
            DisplayDto displayDto = modelMapper.map(display, DisplayDto.class);
            // Modelmapper or Hibernate bug?
            // Sometimes displayDto.displayContent is null, mapping it separately seems to
            // help

            if (display.getDisplayContent() != null)
                displayDto.setDisplayContent(modelMapper.map(display.getDisplayContent(), DisplayContentDto.class));

            DisplayContent currentContent = display.getCurrentContent();
            if (currentContent != null)
                displayDto.setCurrentImageHash(currentContent.getImageHash());

            dtoList.add(displayDto);
        }
        logger.debug("All displays requested");
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Object> createDisplay(@RequestBody DisplayDto displayDto) throws IOException {

        Display display = modelMapper.map(displayDto, Display.class);
        if (displayDto.getDisplayContent() == null) {
            display.setDisplayContent(new DisplayContent());
            displayContentRepository.saveAndFlush(display.getDisplayContent());
            // display.setDisplayContent(templateContent(display).getDisplayContent());

        }
        ResolutionDto resolutionDto = displayDto.getResolution();
        if (resolutionDto != null) {
            Resolution resolution = resolutionRepository.findByWidthAndHeightAndBitDepth(resolutionDto.getWidth(),
                    resolutionDto.getHeight(), resolutionDto.getBitDepth());
            if (resolution != null)
                display.setResolution(resolution);
            else {
                Resolution newResolution = new Resolution();
                newResolution.setWidth(resolutionDto.getWidth());
                newResolution.setHeight(resolutionDto.getHeight());
                newResolution.setBitDepth(resolutionDto.getBitDepth());
                display.setResolution(newResolution);
            }
        }
        TemplateDto templateDto = displayDto.getTemplate();
        if (templateDto != null) {
            Template template = templateRepository.findByUuid(templateDto.getUuid());
            if (template != null) {
                display.setTemplate(template);
            }
        }

        try {
            display = displayRepository.saveAndFlush(display);

        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause != null)
                return new ResponseEntity<>(rootCause.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
            else
                throw (e);
        }

        logger.debug("Display with uuid:" + display.getUuid() + " created.");
        return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/delete/{displayUuid}", method = RequestMethod.DELETE)
    public ResponseEntity deleteDisplay(@PathVariable("displayUuid") String displayUuid) {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Deletion of display with uuid:" + displayUuid + " failed.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        displayRepository.delete(display);
        logger.debug("Deleted display with uuid:" + displayUuid);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/update/", method = RequestMethod.PUT)
    public ResponseEntity<Object> updateDisplay(@RequestBody DisplayDto displayDto) throws IOException {
        Display display = displayRepository.findByUuid(displayDto.getUuid());
        if (display == null) {
            logger.debug("Update display with uuid:" + displayDto.getUuid() + " failed. Display not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResolutionDto resolutionDto = displayDto.getResolution();
        if (resolutionDto != null) {
            Resolution resolution = resolutionRepository.findByWidthAndHeightAndBitDepth(resolutionDto.getWidth(),
                    resolutionDto.getHeight(), resolutionDto.getBitDepth());
            if (resolution != null)
                display.setResolution(resolution);
            else {
                Resolution newResolution = new Resolution();
                newResolution.setWidth(resolutionDto.getWidth());
                newResolution.setHeight(resolutionDto.getHeight());
                newResolution.setBitDepth(resolutionDto.getBitDepth());
                display.setResolution(newResolution);
            }
        }
        TemplateDto templateDto = displayDto.getTemplate();
        if (templateDto != null) {
            Template template = templateRepository.findByUuid(templateDto.getUuid());
            if (template != null) {
                display.setTemplate(template);
            }
        }

        display.setBatteryPercentage(displayDto.getBatteryPercentage());
        display.setName(displayDto.getName());
        display.setLastState(displayDto.getLastState());
        display.setErrorMessage(displayDto.getErrorMessage());
        display.setIgnoreScheduledContent(displayDto.getIgnoreScheduledContent());
        display.setWarningMessage(displayDto.getWarningMessage());
        display.setRoomCodes(displayDto.getRoomCodes());
        display.setLastUpdate(new Date());

        // templateContent(display);

        try {
            display = displayRepository.saveAndFlush(display);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause != null)
                return new ResponseEntity<>(rootCause.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
            else
                throw (e);
        }

        logger.debug("Updated display with uuid:" + display.getUuid());
        return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/sync-status/{displayUuid}", consumes = "application/json")
    public ResponseEntity<String> syncDisplayStatus(@PathVariable("displayUuid") String displayUuid,
            @RequestBody DisplayStateDto stateDto) throws IOException, NoSuchAlgorithmException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Cannot find a Display with uuid:" + displayUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        display.setBatteryPercentage(stateDto.getBatteryPercentage());
        display.setErrorMessage(stateDto.getErrorMessage());
        display.setWarningMessage(stateDto.getWarningMessage());
        display.setLastState(new Date());
        displayRepository.saveAndFlush(display);
        // new
        DisplayContent displayContent = display.getCurrentContent();
        String key = null;
        if (displayContent == null) {
            if (fileImportStorageS3.download(display.getDisplayContent().getUuid()) != null) {
                key = display.getDisplayContent().getUuid();
                displayContent = display.getDisplayContent();
            } else {
                logger.debug("Display with uuid " + displayUuid + " has no image.");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            key = displayContent.getUuid();
        }
        String imageHash = displayContent.getImageHash();
        byte[] image = fileImportStorageS3.download(key);
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bImage = ImageIO.read(is);
        image = imageUtil.convertToByteArray(bImage, true, display.getResolution());
        String generatedHash = imageUtil.convertToMD5Hash(image);

        if (!generatedHash.equals(imageHash)) {
            displayContent.setImageHash(null);
            displayContentRepository.saveAndFlush(displayContent);
        }

        if (imageHash == null) {
            imageHash = "no-hash";
        }

        logger.debug("Status updated and image hash returned for display with uuid:" + displayUuid);
        return new ResponseEntity<>(imageHash, HttpStatus.OK);

    }

    @GetMapping(value = "/get-image/{displayUuid}")
    public ResponseEntity<byte[]> getDisplayImage(@PathVariable("displayUuid") String displayUuid,
            @RequestParam(value = "convertToBMP", required = false) boolean convertToBMP)
            throws IOException, NoSuchAlgorithmException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid " + displayUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // checkTemplateChanged(display);
        DisplayContent displayContent = display.getCurrentContent();

        if (displayContent == null) {
            logger.debug("Display with uuid " + displayUuid + " has no image.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String key = displayContent.getUuid();
        byte[] image = fileImportStorageS3.download(key);
        if (image != null) {
            InputStream is = new ByteArrayInputStream(image);
            BufferedImage bImage = ImageIO.read(is);
            image = imageUtil.convertToByteArray(bImage, convertToBMP, display.getResolution());
        }
        // Set MD5 hash for Display if the image is in native format if

        if (convertToBMP) {
            displayContent.setImageHash(imageUtil.convertToMD5Hash(image));
            displayContentRepository.saveAndFlush(displayContent);
        }

        logger.debug("Get display image with uuid: " + displayUuid);
        return new ResponseEntity<>(image, HttpStatus.OK);

    }

    public void checkTemplateChanged(Display display) throws IOException {
        if (display.getDisplayContent() != null
                && display.getDisplayContent().getLastUpdate().before(display.getTemplate().getLastUpdate())) {
            templateContent1(display);
            display = displayRepository.saveAndFlush(display);
        }
    }

    public Display templateContent1(Display display) throws IOException {
        List<ImageField> imageFields = new ArrayList<>();
        for (ImageField imageField : display.getTemplate().getDisplayContent().getImageFields()) {
            if (display.getTemplate().isMultipleRoom() && !imageField.isRepeat() && !imageField.isRepeated()) {
                imageFields.add(copy(imageField, imageField.getCustomText().toString()));
            } else if (imageField.getFieldType().toString().equals("OTHER")) {
                imageFields.add(copy(imageField, imageField.getCustomText()));
            }
        }
        display.getDisplayContent().setImageFields(imageFields);

        if (imageFields.size() > 0) {
            display.getDisplayContent().setImageFields(imageFields);
            ImageUtil imageUtil = new ImageUtil();
            display.getDisplayContent()
                    .setImageBase64(imageUtil.drawImageTextFields(display.getDisplayContent().getImageFields(),
                            display.getResolution().getWidth(), display.getResolution().getHeight(),
                            display.getTemplate()));

            BufferedImage bImageFromConvert = null;
            byte[] imageBytes = Base64.getDecoder().decode(display.getDisplayContent().getImageBase64());
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            bImageFromConvert = ImageIO.read(bis);
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert),
                    display.getDisplayContent().getUuid());
        } else {
            display.getDisplayContent().setImageBase64(null);
        }
        display.getDisplayContent().setDisplay(display);
        display.getDisplayContent().setLastUpdate(new Date());
        return display;
    }

    private ImageField copy(ImageField imageField, String text) {
        ImageField imageFieldNew = new ImageField();
        imageFieldNew.setBold(imageField.isBold());
        imageFieldNew.setBorder(imageField.isBorder());
        imageFieldNew.setInvert(imageField.isInvert());
        imageFieldNew.setCreated(imageField.getCreated());
        imageFieldNew.setCurrentFieldValue(imageField.getCurrentFieldValue());
        imageFieldNew.setCustomText(text);
        imageFieldNew.setDisplayContent(imageField.getDisplayContent());
        imageFieldNew.setFieldType(imageField.getFieldType());
        imageFieldNew.setFontSize(imageField.getFontSize());
        imageFieldNew.setHeight(imageField.getHeight());
        imageFieldNew.setImage(imageField.getImage());
        imageFieldNew.setItalic(imageField.isItalic());
        imageFieldNew.setLastUpdate(new Date());
        imageFieldNew.setRepeat(imageField.isRepeat());
        imageFieldNew.setRepeated(imageField.isRepeated());
        imageFieldNew.setWidth(imageField.getWidth());
        imageFieldNew.setxPos(imageField.getxPos());
        imageFieldNew.setyPos(imageField.getyPos());
        return imageFieldNew;

    }
}
