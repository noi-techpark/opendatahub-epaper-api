package it.noi.edisplay.controller;


import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.ConnectionRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.services.EDisplayRestService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * Controller class to create API for CRUD operations on Displays
 */
@RestController
@RequestMapping("/display")
public class DisplayController {


	@Autowired
	DisplayRepository displayRepository;

	@Autowired
	TemplateRepository templateRepository;

	@Autowired
	ConnectionRepository connectionRepository;

	@Autowired
	ResolutionRepository resolutionRepository;


	@Autowired
	ModelMapper modelMapper;

	@Autowired
	EDisplayRestService eDisplayRestService;

	Logger logger = LoggerFactory.getLogger(DisplayController.class);

	@RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<DisplayDto> getDisplay(@PathVariable("uuid") String uuid) {
		Display display = displayRepository.findByUuid(uuid);

		if (display == null) {
			logger.debug("Display with uuid: " + uuid + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		logger.debug("Get display with uuid: " + uuid);
		return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.OK);
	}


	@RequestMapping(value = "/send", method = RequestMethod.POST)
	public ResponseEntity send(@RequestParam("uuid") String uuid, @RequestParam("inverted") Boolean inverted) throws IOException {
		Display display = displayRepository.findByUuid(uuid);
		if (display != null) {
			Connection connection = connectionRepository.findByDisplay(display);
			if (connection != null) {
				logger.debug("Sending image to display with uuid:" + uuid);
				StateDto currentState = eDisplayRestService.sendImageToDisplay(display, connection, inverted);
				logger.debug("Image successful send to display with uuid " + uuid);
				return new ResponseEntity(currentState,HttpStatus.OK);
			} else
				logger.debug("Sending image to display with uuid:" + uuid + " failed. Connection not found");
		} else
			logger.debug("Sending image to display with uuid:" + uuid + " failed. Display not found");
		return new ResponseEntity(HttpStatus.BAD_REQUEST);

	}

	@RequestMapping(value = "/state/{uuid}", method = RequestMethod.GET)
	public ResponseEntity getState(@PathVariable("uuid") String uuid) throws IOException {
		Display display = displayRepository.findByUuid(uuid);
		if (display != null) {
			Connection connection = connectionRepository.findByDisplay(display);
			if (connection != null) {
				display.setLastState(new Date());
				displayRepository.save(display);
				StateDto currentState = eDisplayRestService.getCurrentState(connection);
				currentState.setLastState(display.getLastState());
				logger.debug("Get state of display with uuid:" + uuid);
				return new ResponseEntity<>(currentState, HttpStatus.OK);
			} else
				logger.debug("Get state of display with uuid:" + uuid + " failed. Connection not found");
		} else
			logger.debug("Get state of display with uuid:" + uuid + " failed. Display not found");
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/clear/{uuid}", method = RequestMethod.POST)
	public ResponseEntity clear(@PathVariable("uuid") String uuid) {
		Display display = displayRepository.findByUuid(uuid);
		if (display != null) {
			Connection connection = connectionRepository.findByDisplay(display);
			if (connection != null) {
				logger.debug("Clear display with uuid:" + uuid);
				StateDto currentState = eDisplayRestService.clearDisplay(connection);
				return new ResponseEntity(currentState,HttpStatus.OK);
			} else
				logger.debug("Failed to clear display with uuid:" + uuid + ". Connection not found");
		} else
			logger.debug("Failed to clear display with uuid:" + uuid + ". Display not found");
		return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
	public ResponseEntity createDisplay(@RequestParam("name") String name, @RequestParam("templateUuid") String templateUuid, @RequestParam("width") int width, @RequestParam("height") int height) {
		Display display = new Display();
		display.setName(name);
		display.setBatteryPercentage(new Random().nextInt(99));

		Template template = templateRepository.findByUuid(templateUuid);

		if (template != null)
			display.setImage(template.getImage());
		else {
			logger.debug("Display creation failed. Template not found");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Resolution resolutionbyWidthAndHeight = resolutionRepository.findByWidthAndHeight(width, height);
		if (resolutionbyWidthAndHeight == null) {
			Resolution resolution = new Resolution(width, height);
			resolutionRepository.saveAndFlush(resolution);
			display.setResolution(resolution);
		} else
			display.setResolution(resolutionbyWidthAndHeight);

		Display savedDisplay = displayRepository.saveAndFlush(display);

		logger.debug("Display with uuid:" + savedDisplay.getUuid() + " created.");
		return new ResponseEntity<>(modelMapper.map(savedDisplay, DisplayDto.class), HttpStatus.CREATED);
	}

	@RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
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

	@RequestMapping(value = "/update/{templateUuid}", method = RequestMethod.PUT)
	public ResponseEntity updateDisplay(@RequestBody DisplayDto displayDto, @PathVariable("templateUuid") String templateUuid) {
		Display display = displayRepository.findByUuid(displayDto.getUuid());
		if (display == null) {
			logger.debug("Update display with uuid:" + displayDto.getUuid() + " failed. Display not found.");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		display.setBatteryPercentage(displayDto.getBatteryPercentage());

		Template template = templateRepository.findByUuid(templateUuid);
		if (template == null) {
			logger.debug("Update display with uuid:" + displayDto.getUuid() + " failed. Template not found.");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		display.setImage(template.getImage());

		Resolution resolutionbyWidthAndHeight = resolutionRepository.findByWidthAndHeight(displayDto.getResolution().getWidth(), displayDto.getResolution().getHeight());
		if (resolutionbyWidthAndHeight == null) {
			Resolution resolution = new Resolution(displayDto.getResolution().getWidth(), displayDto.getResolution().getHeight());
			resolutionRepository.saveAndFlush(resolution);
			display.setResolution(resolution);
		} else
			display.setResolution(resolutionbyWidthAndHeight);


		display.setName(displayDto.getName());
		display.setLastState(displayDto.getLastState());
		logger.debug("Updated display with uuid:" + display.getUuid());
		return new ResponseEntity(modelMapper.map(displayRepository.saveAndFlush(display), DisplayDto.class), HttpStatus.ACCEPTED);
	}

}
