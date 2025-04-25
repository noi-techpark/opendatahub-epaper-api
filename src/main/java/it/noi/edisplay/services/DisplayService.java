// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.noi.edisplay.components.NOIDataLoader;
import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.DisplayStateDto;
import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.ImageField;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.DisplayContentRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

/**
 * Service class for Display Controller for all CRUD logic on Displays
 */
@Service
public class DisplayService {

    @Value("${event.advance}")
	private int eventAdvance;

	@Value("${event.offset}")
	private int eventOffset;

    private final ModelMapper modelMapper;
    private final DisplayContentRepository displayContentRepository;
    private final DisplayRepository displayRepository;
    private final TemplateRepository templateRepository;
    private final ResolutionRepository resolutionRepository;
    private final NOIDataLoader noiDataLoader;
    private final FileImportStorageS3 fileImportStorageS3;
    private final ImageUtil imageUtil;

    private final Logger logger = LoggerFactory.getLogger(DisplayService.class);

    public DisplayService(
        ModelMapper modelMapper,
        DisplayRepository displayRepository,
        DisplayContentRepository displayContentRepository,
        TemplateRepository templateRepository,
        ResolutionRepository resolutionRepository,
        NOIDataLoader noiDataLoader,
        FileImportStorageS3 fileImportStorageS3,
        ImageUtil imageUtil
    ) {
        this.modelMapper = modelMapper;
        this.displayRepository = displayRepository;
        this.displayContentRepository = displayContentRepository;
        this.templateRepository = templateRepository;
        this.resolutionRepository = resolutionRepository;
        this.noiDataLoader = noiDataLoader;
        this.fileImportStorageS3 = fileImportStorageS3;
        this.imageUtil = imageUtil;
    }

    public ResponseEntity<DisplayDto> getDisplay(String uuid) {
        Display display = displayRepository.findByUuid(uuid);
        if (display == null) {
            logger.debug("Display with uuid: " + uuid + " not found!");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.debug("Get display with uuid: {}", uuid);
        return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.OK);
    }

    public ResponseEntity<List<DisplayDto>> getAllDisplays() {
        List<Display> list = displayRepository.findAll();
        ArrayList<DisplayDto> displayDtos = new ArrayList<>();
        for (Display display : list) {
            DisplayDto displayDto = modelMapper.map(display, DisplayDto.class);

            if (display.getDisplayContent() != null) {
                displayDto.setDisplayContent(modelMapper.map(display.getDisplayContent(), DisplayContentDto.class));
            } else {
                displayDto.setDisplayContent(null);
            }

            DisplayContent currentContent = display.getCurrentContent();
            if (currentContent != null) {
                displayDto.setCurrentImageHash(currentContent.getImageHash());
            }
            displayDtos.add(displayDto);
        }
        logger.debug("All displays requesterd");
        return new ResponseEntity<>(displayDtos, HttpStatus.OK);
    }

    public ResponseEntity<Object> createDisplay(DisplayDto displayDto) {
        Display display = modelMapper.map(displayDto, Display.class);
        if (displayDto.getDisplayContent() == null) {
            display.setDisplayContent(null);
        }

        ResolutionDto resolutionDto = displayDto.getResolution();
        if (resolutionDto != null) {
            Resolution resolution = resolutionRepository.findByWidthAndHeightAndBitDepth(
                resolutionDto.getWidth(), 
                resolutionDto.getHeight(), 
                resolutionDto.getBitDepth());
            if (resolution != null) {
                display.setResolution(resolution);
            } else {
                Resolution newResolution = new Resolution();
                newResolution.setWidth(resolutionDto.getWidth());
                newResolution.setHeight(resolutionDto.getHeight());
                newResolution.setBitDepth(resolutionDto.getBitDepth());
                display.setResolution(newResolution);
            }
        }
        try {
            display = displayRepository.saveAndFlush(display);
        } catch (DataIntegrityViolationException e) {
            Throwable roootCause = e.getRootCause();
            if (roootCause != null) {
                return new ResponseEntity<>(roootCause.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
            } else {
                throw (e);
            }
        }
        logger.debug("Display with uuid: {} created!", display.getUuid());
        return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteDisplay(String displayUuid) {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid: {} doesn't exist!", displayUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        displayRepository.delete(display);
        logger.debug("Display with uuid: {}, deleted successfully!", displayUuid);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<Object> updateDisplay(DisplayDto displayDto) {
        Display display = displayRepository.findByUuid(displayDto.getUuid());
        if (display == null) {
            logger.debug("Display with uuid: {}, doesn't exist!", displayDto.getUuid());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ResolutionDto resolutionDto = displayDto.getResolution();
        if (resolutionDto != null) {
            Resolution resolution = resolutionRepository.findByWidthAndHeightAndBitDepth(
                resolutionDto.getWidth(), 
                resolutionDto.getHeight(), 
                resolutionDto.getBitDepth());
            if (resolution != null) {
                display.setResolution(resolution);
            } else {
                Resolution newResolution = new Resolution();
                newResolution.setWidth(resolutionDto.getWidth());
                newResolution.setHeight(resolutionDto.getHeight());
                newResolution.setBitDepth(resolutionDto.getBitDepth());
                display.setResolution(newResolution);
            }
        }

        display.setBatteryPercentage(displayDto.getBatteryPercentage());
        display.setName(displayDto.getName());
        display.setLastState(displayDto.getLastState());
        display.setErrorMessage(displayDto.getErrorMessage());
        display.setIgnoreScheduledContent(displayDto.getIgnoreScheduledContent());
        display.setWarningMessage(displayDto.getWarningMessage());
        display.setRoomCodes(displayDto.getRoomCodes());

        try {
            display = displayRepository.saveAndFlush(display);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();
            if (rootCause != null) {
                return new ResponseEntity<>(rootCause.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
            } else {
                throw (e);
            }
        }

        logger.debug("Updated display with uuid: {}", display.getUuid());
        return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.ACCEPTED);
    }

    public ResponseEntity<String> syncDisplayStatus(String displayUuid, DisplayStateDto stateDto) throws IOException, NoSuchAlgorithmException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid: {}, doesn't exist!", displayUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        display.setBatteryPercentage(stateDto.getBatteryPercentage());
        display.setErrorMessage(stateDto.getErrorMessage());
        display.setWarningMessage(stateDto.getWarningMessage());
        display.setLastState(new Date());

        displayRepository.saveAndFlush(display);

        DisplayContent displayContent = display.getCurrentContent();
        if (displayContent == null) {
            logger.debug("Display with uuid {} has no content!", displayContent);
            return new ResponseEntity<>("Display content is missing!", HttpStatus.NOT_FOUND);
        }

        String imageHash = displayContent.getImageHash();

        byte[] image = fileImportStorageS3.download(displayContent.getUuid());
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bImage = ImageIO.read(is);

        Map<String, List<EventDto>> noiDisplayEventsByRoom = noiDataLoader.getNOIDisplayEventsByRoom(display);

        List<ImageField> imageFields = displayContent.getImageFields();

        imageUtil.drawDisplayImage(
            display, 
            displayContent, 
            bImage, 
            noiDisplayEventsByRoom, 
            imageFields, 
            eventAdvance + eventOffset
        );
        
        image = imageUtil.convertToByteArray(bImage, true, display.getResolution());

        String generatedHash = imageUtil.convertToMD5Hash(image);

        if (!generatedHash.equals(imageHash)) {
            displayContent.setImageHash(null);
            displayContentRepository.saveAndFlush(displayContent);
        }

        if (imageHash == null) {
            imageHash = "no-hash";
        }

        logger.debug("Status updated and image hash returned for display with uuid: {}!", displayUuid);
        return new ResponseEntity<>(imageHash, HttpStatus.OK);
    }

    public ResponseEntity<byte[]> getDisplayImage(String displayUuid, boolean convertToBMP, boolean withTextFields) throws IOException, NoSuchAlgorithmException  {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid: {} wasn't found!", displayUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContent displayContent = display.getCurrentContent();

        if (displayContent == null) {
            logger.debug("Display with uuid {} has not image!", displayUuid);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        byte[] image = fileImportStorageS3.download(displayContent.getUuid());
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bImage = ImageIO.read(is);

        if(withTextFields) {
            Map<String, List<EventDto>> noiDisplayEventsByRoom = noiDataLoader.getNOIDisplayEventsByRoom(display);
            List<ImageField> imageFields = displayContent.getImageFields();
            imageUtil.drawDisplayImage(
                display, 
                displayContent, 
                bImage, 
                noiDisplayEventsByRoom, 
                imageFields, 
                eventAdvance + eventOffset
            );
        }
        image = imageUtil.convertToByteArray(bImage, convertToBMP, display.getResolution());

        // Set MD5 hash for display if the image is in native format
        if (convertToBMP) {
            displayContent.setImageHash(imageUtil.convertToMD5Hash(image));
            displayContentRepository.saveAndFlush(displayContent);
        }

        logger.debug("Get image of display with uuid: {}", displayUuid);
            
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    public ResponseEntity<DisplayContentDto> setDisplayContent(String displayUuid, String displayContentDtoJson, MultipartFile image) throws IOException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with {} was not found!", displayUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContentDto displayContentDto = new ObjectMapper().readValue(displayContentDtoJson, DisplayContentDto.class);
        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean displayContentExists = display.getDisplayContent() != null;

        if (!displayContentExists) {
            display.setDisplayContent(new DisplayContent());
            display.getDisplayContent().setDisplay(display);
        }
        display.getDisplayContent().setImageFields(displayContent.getImageFields());
        display.getDisplayContent().setDisplay(displayContent.getDisplay());
        

        if (image != null) {
            InputStream is = new ByteArrayInputStream(image.getBytes());
            BufferedImage bImage = ImageIO.read(is);
            String fileKey = display.getDisplayContent().getUuid();
            fileImportStorageS3.upload(imageUtil.convertToMonochrome(bImage), fileKey);
        }

        // display content has changed, so the current image hash is no longer valid......
        display.getDisplayContent().setImageHash(null);

        Display savedDisplay = displayRepository.saveAndFlush(display);


        if (displayContentExists) {
            logger.debug("Updated image for Display uuid: {}", displayUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created image for display with uuid: {}", displayUuid);
        return new ResponseEntity<>(modelMapper.map(savedDisplay.getDisplayContent(), DisplayContentDto.class), HttpStatus.CREATED);
    }

    public ResponseEntity<DisplayContentDto> setDisplayContentByTemplate(String displayUuid, String templateUuid, DisplayContentDto displayContentDto) throws JsonProcessingException {
        Display display = displayRepository.findByUuid(displayUuid);

        if (display == null) {
            logger.debug("Display with uuid: {} was not found!", displayUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Template template = templateRepository.findByUuid(templateUuid);

        if (template == null) {
            logger.debug("Template with uuid: {} was not found!", templateUuid);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        DisplayContent displayContent = modelMapper.map(displayContentDto, DisplayContent.class);

        boolean displayContentExists = display.getDisplayContent() != null;
        if (!displayContentExists) {
            display.setDisplayContent(new DisplayContent());
            display.getDisplayContent().setDisplay(display);
        }

        if (template.getDisplayContent() != null) {
            // copy background image from template
            fileImportStorageS3.copy(template.getDisplayContent().getUuid(), display.getDisplayContent().getUuid());
        }

        display.getDisplayContent().setImageFields(displayContent.getImageFields());
        display.getDisplayContent().setPadding(displayContent.getPadding());

        // Display content has changed, so the current image hash is no longer valid
        display.getDisplayContent().setImageHash(null);

        Display savedDisplay = displayRepository.saveAndFlush(display);

        if (displayContentExists) {
            logger.debug("Updated image for display uuid: {}", displayUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        logger.debug("Created image for Display uuid: {}", displayUuid);
        
        return new ResponseEntity<>(modelMapper.map(savedDisplay.getDisplayContent(), DisplayContentDto.class), HttpStatus.CREATED);
    }
    
}
