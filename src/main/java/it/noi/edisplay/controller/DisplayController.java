// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import it.noi.edisplay.components.NOIDataLoader;
import it.noi.edisplay.dto.DisplayContentDto;
import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.DisplayStateDto;
import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.dto.ResolutionDto;
import it.noi.edisplay.model.*;
import it.noi.edisplay.repositories.*;
import it.noi.edisplay.storage.FileImportStorageS3;
import it.noi.edisplay.utils.ImageUtil;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Controller class to create API for CRUD operations on Displays
 */
@RestController
@RequestMapping("/display")
public class DisplayController {

	@Value("${event.advance}")
	private int eventAdvance;

	@Value("${event.offset}")
	private int eventOffset;

	@Autowired
	private DisplayRepository displayRepository;

	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private ResolutionRepository resolutionRepository;

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
	public ResponseEntity<Object> createDisplay(@RequestBody DisplayDto displayDto) {

		Display display = modelMapper.map(displayDto, Display.class);
		if (displayDto.getDisplayContent() == null) {
			display.setDisplayContent(null);
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
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		displayRepository.delete(display);
		logger.debug("Deleted display with uuid:" + displayUuid);

		return new ResponseEntity(HttpStatus.OK);
	}

	@RequestMapping(value = "/update/", method = RequestMethod.PUT)
	public ResponseEntity<Object> updateDisplay(@RequestBody DisplayDto displayDto) {
		Display display = displayRepository.findByUuid(displayDto.getUuid());
		if (display == null) {
			logger.debug("Update display with uuid:" + displayDto.getUuid() + " failed. Display not found.");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
			if (rootCause != null)
				return new ResponseEntity<>(rootCause.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
			else
				throw (e);
		}

		logger.debug("Updated display with uuid:" + display.getUuid());
		return new ResponseEntity(modelMapper.map(display, DisplayDto.class), HttpStatus.ACCEPTED);
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

		DisplayContent displayContent = display.getCurrentContent();
		String imageHash = displayContent.getImageHash();

		byte[] image = fileImportStorageS3.download(displayContent.getUuid());
		InputStream is = new ByteArrayInputStream(image);
		BufferedImage bImage = ImageIO.read(is);

		Map<String, List<EventDto>> noiDisplayEventsByRoom = noiDataLoader.getNOIDisplayEventsByRoom(display);

		List<ImageField> imageFields = displayContent.getImageFields();

		imageUtil.drawDisplayImage(display, displayContent, bImage, noiDisplayEventsByRoom, imageFields, eventAdvance + eventOffset);

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
			@RequestParam(value = "convertToBMP", required = false) boolean convertToBMP, boolean withTextFields)
			throws IOException, NoSuchAlgorithmException {
		Display display = displayRepository.findByUuid(displayUuid);

		if (display == null) {
			logger.debug("Display with uuid " + displayUuid + " was not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		DisplayContent displayContent = display.getCurrentContent();

		if (displayContent == null) {
			logger.debug("Display with uuid " + displayUuid + " has no image.");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		byte[] image = fileImportStorageS3.download(displayContent.getUuid());
		InputStream is = new ByteArrayInputStream(image);
		BufferedImage bImage = ImageIO.read(is);

		if (withTextFields) {
			Map<String, List<EventDto>> noiDisplayEventsByRoom = noiDataLoader.getNOIDisplayEventsByRoom(display);
			List<ImageField> imageFields = displayContent.getImageFields();
			imageUtil.drawDisplayImage(display, displayContent, bImage, noiDisplayEventsByRoom, imageFields,
					eventAdvance + eventOffset);
		}
		image = imageUtil.convertToByteArray(bImage, convertToBMP, display.getResolution());

		// Set MD5 hash for Display if the image is in native format
		if (convertToBMP) {
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
		display.getDisplayContent().setPadding(displayContent.getPadding());

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
		display.getDisplayContent().setPadding(displayContent.getPadding());

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
