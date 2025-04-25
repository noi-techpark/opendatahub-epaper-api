package it.noi.edisplay.services;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import it.noi.edisplay.components.NOIDataLoader;
import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.DisplayContent;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.repositories.DisplayContentRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

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

    public ResponseEntity<String> syncDisplayStatus(String)
    
}
