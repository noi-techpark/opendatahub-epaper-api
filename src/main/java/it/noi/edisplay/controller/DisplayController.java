package it.noi.edisplay.controller;


import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.ConnectionRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.services.EDisplayRestService;
import org.modelmapper.ModelMapper;
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
	ModelMapper modelMapper;

	@Autowired
	EDisplayRestService eDisplayRestService;

	@RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<DisplayDto> getDisplay(@PathVariable("uuid") String uuid) {
		Display display = displayRepository.findByUuid(uuid);

		if (display == null)
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.OK);
	}


	@RequestMapping(value = "/send-to-e-ink-display/{uuid}", method = RequestMethod.POST)
	public void sendImageToEInkDisplay(@PathVariable("uuid") String uuid) throws IOException {
		Display display = displayRepository.findByUuid(uuid);
		if (display != null) {
			Connection connection = connectionRepository.findByDisplay(display);
			if (connection != null)
				eDisplayRestService.sendImageToDisplay(display, connection);
		}
	}

	@RequestMapping(value = "/get-e-ink-display-state/{uuid}", method = RequestMethod.GET)
	public ResponseEntity getEInkDisplayState(@PathVariable("uuid") String uuid) throws IOException {
		Display display = displayRepository.findByUuid(uuid);
		if (display != null) {
			Connection connection = connectionRepository.findByDisplay(display);
			if (connection != null) {
				display.setLastState(new Date());
				displayRepository.save(display);
				StateDto currentState = eDisplayRestService.getCurrentState(connection);
				currentState.setLastState(display.getLastState());
				return new ResponseEntity<>(currentState, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/clear-e-ink-display/{uuid}", method = RequestMethod.POST)
	public void clearEInkDisplay(@PathVariable("uuid") String uuid) {
		Display display = displayRepository.findByUuid(uuid);
		if (display != null) {
			Connection connection = connectionRepository.findByDisplay(display);
			if (connection != null)
				eDisplayRestService.clearDisplay(connection);
		}
	}


	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public ResponseEntity getAllDisplays() {
		List<Display> list = displayRepository.findAll();
		ArrayList<DisplayDto> dtoList = new ArrayList<>();
		for (Display display : list)
			dtoList.add(modelMapper.map(display, DisplayDto.class));
		return new ResponseEntity<>(dtoList, HttpStatus.OK);
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity createDisplay(@RequestParam("name") String name, @RequestParam("templateUuid") String templateUuid) {
		Display display = new Display();
		display.setName(name);
		display.setBatteryPercentage(new Random().nextInt(99));

		Template template = templateRepository.findByUuid(templateUuid);
		display.setImage(template.getImage());

		return new ResponseEntity<>(modelMapper.map(displayRepository.saveAndFlush(display), DisplayDto.class), HttpStatus.CREATED);
	}

	@RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
	public ResponseEntity deleteDisplay(@PathVariable("uuid") String uuid) {
		Display display = displayRepository.findByUuid(uuid);

		if (display == null)
			return new ResponseEntity(HttpStatus.BAD_REQUEST);

		displayRepository.delete(display);
		return new ResponseEntity(HttpStatus.OK);

	}

	@RequestMapping(value = "/update/{templateUuid}", method = RequestMethod.PUT, consumes = "application/json")
	public ResponseEntity updateDisplay(@RequestBody DisplayDto displayDto,@PathVariable("templateUuid") String templateUuid) {
		Display display = displayRepository.findByUuid(displayDto.getUuid());
		if (display == null)
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		display.setBatteryPercentage(displayDto.getBatteryPercentage());

		Template template = templateRepository.findByUuid(templateUuid);
		display.setImage(template.getImage());

		display.setName(displayDto.getName());
		display.setLastState(displayDto.getLastState());
		return new ResponseEntity(modelMapper.map(displayRepository.saveAndFlush(display),DisplayDto.class),HttpStatus.ACCEPTED);
	}

}
