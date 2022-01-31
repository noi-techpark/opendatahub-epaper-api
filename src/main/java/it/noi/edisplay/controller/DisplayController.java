package it.noi.edisplay.controller;

import it.noi.edisplay.components.NOIDataLoader;
import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.DisplayStateDto;
import it.noi.edisplay.model.*;
import it.noi.edisplay.repositories.*;
import it.noi.edisplay.storage.FileImportStorageS3;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Controller class to create API for CRUD operations on Displays
 */
@RestController
@RequestMapping("/display")
public class DisplayController {

    @Autowired
    private DisplayRepository displayRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private ResolutionRepository resolutionRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DisplayContentRepository displayContentRepository;

    @Autowired
    private NOIDataLoader noiDataLoader;

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
        for (Display display : list)
            dtoList.add(modelMapper.map(display, DisplayDto.class));
        logger.debug("All displays requested");
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity createDisplay(@RequestParam("name") String name,
            @RequestParam("templateUuid") String templateUuid, @RequestParam("width") int width,
            @RequestParam("height") int height, @RequestParam("locationUuid") String locationUuid) {
        Display display = new Display();
        display.setName(name);
        display.setBatteryPercentage(new Random().nextInt(99));

        Template template = templateRepository.findByUuid(templateUuid);
        if (template != null)
            display.setTemplate(template);
        else {
            logger.debug("Display creation failed. Template not found");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (locationUuid != null) {
            Location location = locationRepository.findByUuid(locationUuid);
            if (location != null)
                display.setLocation(location);
            else {
                logger.debug("Display creation failed. Location not found");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        Resolution resolutionbyWidthAndHeight = resolutionRepository.findByWidthAndHeight(width, height);
        if (resolutionbyWidthAndHeight == null) {
//			Resolution resolution = new Resolution(width, height);
//			resolutionRepository.saveAndFlush(resolution);
//			display.setResolution(resolution);
        } else
            display.setResolution(resolutionbyWidthAndHeight);

        Display savedDisplay = displayRepository.saveAndFlush(display);

        logger.debug("Display with uuid:" + savedDisplay.getUuid() + " created.");
        return new ResponseEntity<>(modelMapper.map(savedDisplay, DisplayDto.class), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/delete/{displayUuid}", method = RequestMethod.DELETE)
    public ResponseEntity deleteDisplay(@PathVariable("uuid") String uuid) {
        Display display = displayRepository.findByUuid(uuid);

        if (display == null) {
            logger.debug("Deletion of display with uuid:" + uuid + " failed.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        displayRepository.delete(display);
        logger.debug("Deleted display with uuid:" + uuid);
        return new ResponseEntity(HttpStatus.OK);

    }

    @RequestMapping(value = "/update/", method = RequestMethod.PUT)
    public ResponseEntity updateDisplay(@RequestBody DisplayDto displayDto) {
        Display display = displayRepository.findByUuid(displayDto.getUuid());
        if (display == null) {
            logger.debug("Update display with uuid:" + displayDto.getUuid() + " failed. Display not found.");
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        display.setBatteryPercentage(displayDto.getBatteryPercentage());

        if (displayDto.getLocationUuid() != null) {
            Location location = locationRepository.findByUuid(displayDto.getLocationUuid());
            if (location != null)
                display.setLocation(location);
            else {
                logger.debug("Update display with uuid:" + displayDto.getUuid() + " failed. Location not found");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        Resolution resolutionbyWidthAndHeight = resolutionRepository
                .findByWidthAndHeight(displayDto.getResolution().getWidth(), displayDto.getResolution().getHeight());
        if (resolutionbyWidthAndHeight == null) {
//			Resolution resolution = new Resolution(displayDto.getResolution().getWidth(), displayDto.getResolution().getHeight());
//			resolutionRepository.saveAndFlush(resolution);
//			display.setResolution(resolution);
        } else
            display.setResolution(resolutionbyWidthAndHeight);

        display.setName(displayDto.getName());
        display.setLastState(displayDto.getLastState());
        display.setErrorMessage(displayDto.getErrorMessage());
        display.setIgnoringScheduledContent(displayDto.isIgnoringScheduledContent());
        display.setWarningMessage(displayDto.getWarningMessage());

        logger.debug("Updated display with uuid:" + display.getUuid());
        return new ResponseEntity(modelMapper.map(displayRepository.saveAndFlush(display), DisplayDto.class),
                HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/sync-status/{displayUuid}", consumes = "application/json")
    public ResponseEntity<String> syncDisplayStatus(@PathVariable("displayUuid") String displayUuid,
            @RequestBody DisplayStateDto stateDto) {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Cannot find a Display with uuid:" + displayUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        display.setBatteryPercentage(stateDto.getBatteryPercentage());
        display.setErrorMessage(stateDto.getErrorMessage());
        display.setWarningMessage(stateDto.getWarningMessage());

        displayRepository.saveAndFlush(display);

        String imageHash = null;

        // MD5 validation
        DisplayContent displayContent = display.getCurrentDisplayContent();
        if (displayContent != null) {
            imageHash = displayContent.getImageHash();
            if (imageHash != null) {

                // We need to validate the hash by checking if image field values are not out-dated
                Map<ImageFieldType, String> fieldValues = display
                        .getTextFieldValues(noiDataLoader.getNOIDisplayEvents(display));

                for (ImageField field : displayContent.getImageFields()) {
                    if (field.getFieldType() != ImageFieldType.CUSTOM_TEXT && (field.getCurrentFieldValue() == null
                            || !field.getCurrentFieldValue().equals(fieldValues.get(field.getFieldType())))) {
                        // Field value does not match, delete hash
                        imageHash = null;
                        displayContent.setImageHash(null);
                        displayContentRepository.saveAndFlush(displayContent);
                        break;
                    }
                }
            }
        }
        if (imageHash == null) {
            imageHash = "no-hash";
        }

        logger.debug("Status updated and image hash returned for display with uuid:" + displayUuid);
        return new ResponseEntity<>(imageHash, HttpStatus.OK);
    }

    @GetMapping(value = "/get-image/{displayUuid}")
    public ResponseEntity<byte[]> getDisplayImage(@PathVariable("displayUuid") String displayUuid,
            @RequestParam(value = "convertToBMP", required = false) boolean convertToBMP, boolean withTextFields)
            throws IOException, NoSuchAlgorithmException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid " + displayUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContent displayContent = display.getCurrentDisplayContent();

        if (displayContent == null) {
            logger.debug("Display with uuid " + displayUuid + " has no image.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] image = fileImportStorageS3.download(displayContent.getUuid());
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bImage = ImageIO.read(is);

        Map<ImageFieldType, String> fieldValues = null;
        if (withTextFields) {
            fieldValues = display.getTextFieldValues(noiDataLoader.getNOIDisplayEvents(display));
            imageUtil.setImageFields(bImage, displayContent.getImageFields(), fieldValues);
        }
        image = imageUtil.convertToByteArray(bImage, convertToBMP, display.getResolution());

        // Set MD5 hash for Display if the image is in native format
        if (convertToBMP) {
            if (fieldValues != null) {
                // Set current field values for later MD5 validation
                for (ImageField field : displayContent.getImageFields()) {
                    if (field.getFieldType() != ImageFieldType.CUSTOM_TEXT) {
                        field.setCurrentFieldValue(fieldValues.get(field.getFieldType()));
                    }
                }
            }

            displayContent.setImageHash(imageUtil.convertToMD5Hash(image));
            displayContentRepository.saveAndFlush(displayContent);
        }

        logger.debug("Get display image with uuid: " + displayUuid);
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    @PostMapping(value = "/set-new-image/{displayUuid}", consumes = "multipart/form-data")
    public ResponseEntity<DisplayContentDto> setDisplayContent(@PathVariable("displayUuid") String displayUuid,
            @RequestParam("displayContentDtoJson") String displayContentDtoJson,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid " + displayUuid + " was not found.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContentDto displayContentDto = new ObjectMapper().readValue(displayContentDtoJson,
                DisplayContentDto.class);
        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean displayContentExists = display.getDisplayContent() != null;

        if (!displayContentExists) {
            display.setDisplayContent(new DisplayContent());
            display.getDisplayContent().setDisplay(display);
        }
        display.getDisplayContent().setImageFields(displayContent.getImageFields());

        if (image != null) {
            InputStream in = new ByteArrayInputStream(image.getBytes());
            BufferedImage bImageFromConvert = ImageIO.read(in);
            String fileKey = display.getDisplayContent().getUuid();
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImageFromConvert), fileKey);
        }

        // Display content has changed, so the current image hash is no longer valid
        display.getDisplayContent().setImageHash(null);

        Display savedDisplay = displayRepository.saveAndFlush(display);

        if (displayContentExists) {
            logger.debug("Updated image for Display uuid:" + displayUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created image for Display uuid:" + displayUuid);
        return new ResponseEntity<>(modelMapper.map(savedDisplay.getDisplayContent(), DisplayContentDto.class),
                HttpStatus.CREATED);
    }

    @PostMapping(value = "/set-template-image/{displayUuid}")
    public ResponseEntity<DisplayContentDto> setDisplayContentByTemplate(
            @PathVariable("displayUuid") String displayUuid, String templateUuid,
            @RequestBody DisplayContentDto displayContentDto) throws JsonProcessingException {
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

        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean displayContentExists = display.getDisplayContent() != null;
        if (!displayContentExists) {
            display.setDisplayContent(new DisplayContent());
            display.getDisplayContent().setDisplay(display);
        }

        if (template.getDisplayContent() != null) {
            // Copy background image from template
            fileImportStorageS3.copy(template.getDisplayContent().getUuid(), display.getDisplayContent().getUuid());
        }

        display.getDisplayContent().setImageFields(displayContent.getImageFields());

        // Display content has changed, so the current image hash is no longer valid
        display.getDisplayContent().setImageHash(null);

        Display savedDisplay = displayRepository.saveAndFlush(display);

        if (displayContentExists) {
            logger.debug("Updated image for Display uuid:" + displayUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created image for Display uuid:" + displayUuid);
        return new ResponseEntity<>(modelMapper.map(savedDisplay.getDisplayContent(), DisplayContentDto.class),
                HttpStatus.CREATED);
    }
}
