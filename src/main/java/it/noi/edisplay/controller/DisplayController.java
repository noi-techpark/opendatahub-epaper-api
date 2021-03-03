package it.noi.edisplay.controller;


import it.noi.edisplay.dto.DisplayDto;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.*;
import it.noi.edisplay.repositories.*;
import it.noi.edisplay.services.EDisplayRestService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static it.noi.edisplay.components.DefaultDataLoader.EVENT_TEMPLATE_NAME;


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
	private ConnectionRepository connectionRepository;

	@Autowired
	private ResolutionRepository resolutionRepository;

	@Autowired
	private LocationRepository locationRepository;


	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private EDisplayRestService eDisplayRestService;


	private Logger logger = LoggerFactory.getLogger(DisplayController.class);

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
		StateDto currentState;
		if (display != null) {
			Connection connection = connectionRepository.findByDisplay(display);
			if (connection != null) {
				logger.debug("Sending image to display with uuid:" + uuid);
				currentState = eDisplayRestService.sendImageToDisplay(connection, inverted);
				display.setLastState(new Date());
				displayRepository.save(display);
				currentState.setLastState(display.getLastState());
				logger.debug("Image successful send to display with uuid " + uuid);
				return new ResponseEntity(currentState, HttpStatus.OK);
			} else {
				logger.debug("Sending image to display with uuid:" + uuid + " failed. Connection not found");
				currentState = new StateDto("No connection found");
				currentState.setLastState(display.getLastState());
			}
		} else {
			logger.debug("Sending image to display with uuid:" + uuid + " failed. Display not found");
			currentState = new StateDto("No display found");
		}
		return new ResponseEntity(currentState, HttpStatus.BAD_REQUEST);

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
				display.setLastState(new Date());
				displayRepository.save(display);
				StateDto currentState = eDisplayRestService.clearDisplay(connection);
				currentState.setLastState(display.getLastState());
				return new ResponseEntity(currentState, HttpStatus.OK);
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
			display.setTemplate(template);
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

	@RequestMapping(value = "/auto-create", method = RequestMethod.POST)
	public ResponseEntity autoCreateDisplay(@RequestParam("name") String name, @RequestParam("ip") String ip, @RequestParam("width") int width, @RequestParam("height") int height, @RequestParam("mac") String mac) throws IOException {

		Connection connectionByMac = connectionRepository.findByMac(mac);

		if (connectionByMac == null) {
			//check if name already exists and connect with that display

			Display displayByName = displayRepository.findByName(name);

			if (displayByName != null) {
				logger.debug("AUTO-CREATE: RECONNECT TO DISPLAY WITH NAME " + name);

				Connection connectionByDisplay = connectionRepository.findByDisplay(displayByName);
				connectionByDisplay.setMac(mac);
				connectionByDisplay.setNetworkAddress(ip);

				connectionRepository.save(connectionByDisplay);

				logger.debug("AUTO-CREATE: FINISHED WITH NEW IP " + ip);

				StateDto state = eDisplayRestService.sendImageToDisplay(connectionByDisplay, false);
				if (state.getErrorMessage() != null) {
					connectionByDisplay.setConnected(false);
					logger.debug("Trying to connect to physical display failed with error: " + state.getErrorMessage());
				} else {
					connectionByDisplay.setConnected(true);
					logger.debug("AUTO-CREATE: Image sent");
				}

				connectionRepository.save(connectionByDisplay);
				logger.debug("AUTO-CREATE: COMPLETED");
			} else {

				logger.debug("AUTO-CREATE: CREATE STARTED");
				Display display = new Display();
				display.setName(name);
				display.setBatteryPercentage(new Random().nextInt(99));

				display.setTemplate(templateRepository.findByName(EVENT_TEMPLATE_NAME));

				Resolution resolutionbyWidthAndHeight = resolutionRepository.findByWidthAndHeight(width, height);
				if (resolutionbyWidthAndHeight == null) {
					Resolution resolution = new Resolution(width, height);
					resolutionRepository.saveAndFlush(resolution);
					display.setResolution(resolution);
				} else
					display.setResolution(resolutionbyWidthAndHeight);

				Display savedDisplay = displayRepository.saveAndFlush(display);

				logger.debug("AUTO-CREATE: Display with uuid:" + savedDisplay.getUuid() + " created.");

				Connection connection = new Connection();
				Location location = locationRepository.findByName("Meeting Room");

				connection.setDisplay(savedDisplay);
				connection.setNetworkAddress(ip);
				connection.setLocation(location);
				connection.setMac(mac);
				connection.setCoordinates(new Point(0, 0));

				StateDto state = eDisplayRestService.sendImageToDisplay(connection, false);
				if (state.getErrorMessage() != null) {
					connection.setConnected(false);
					logger.debug("Trying to connect to physical display failed with error: " + state.getErrorMessage());
				} else {
					connection.setConnected(true);
					logger.debug("AUTO-CREATE: Image sent to:" + savedDisplay.getUuid());
				}

				Connection savedConnection = connectionRepository.save(connection);
				logger.debug("AUTO-CREATE: Connection with uuid:" + savedConnection.getUuid() + " created.");
			}
		} else {
			logger.debug("AUTO-CREATE: RECONNECT BY MAC ADDRESS STARTED");
			connectionByMac.setNetworkAddress(ip);
			StateDto state = eDisplayRestService.sendImageToDisplay(connectionByMac, false);
			if (state.getErrorMessage() != null) {
				connectionByMac.setConnected(false);
				logger.debug("Trying to connect to physical display failed with error: " + state.getErrorMessage());
			} else {
				connectionByMac.setConnected(true);
				connectionByMac.setNetworkAddress(ip);
				logger.debug("AUTO-CREATE: Connection with uuid:" + connectionByMac.getUuid() + " has new IP " + ip);
			}
			connectionRepository.save(connectionByMac);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}


	@RequestMapping(value = "/simple-create", method = RequestMethod.POST)
	public ResponseEntity simpleCreateDisplay(@RequestParam("name") String name, @RequestParam("templateUuid") String templateUuid, @RequestParam("width") int width, @RequestParam("height") int height, @RequestParam("networkAddress") String networkAddress, @RequestParam("locationUuid") String locationUuid) {
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

		Resolution resolutionbyWidthAndHeight = resolutionRepository.findByWidthAndHeight(width, height);
		if (resolutionbyWidthAndHeight == null) {
			Resolution resolution = new Resolution(width, height);
			resolutionRepository.saveAndFlush(resolution);
			display.setResolution(resolution);
		} else
			display.setResolution(resolutionbyWidthAndHeight);

		Display savedDisplay = displayRepository.saveAndFlush(display);

		logger.debug("Display with uuid:" + savedDisplay.getUuid() + " created.");

		Location location = locationRepository.findByUuid(locationUuid);

		Connection connection = connectionRepository.save(new Connection(savedDisplay, location, new Point(0, 0), networkAddress));
		logger.debug("Connection with uuid:" + connection.getUuid() + " created.");
		return new ResponseEntity<>(modelMapper.map(display, DisplayDto.class), HttpStatus.OK);
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
		display.setTemplate(template);

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

	@RequestMapping(value = "/testproxyconnection", method = RequestMethod.GET)
	public ResponseEntity<String> test() {
		logger.debug("Connection with with Proxy established.");
		String response = eDisplayRestService.testProxy();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/proxy-register", method = RequestMethod.POST)
	public ResponseEntity<String> proxyRegister(@RequestParam("url") String url) {
		logger.debug("Registering new proxy URL: " + url);
		eDisplayRestService.setProxyIpAddress(url);
		String response = eDisplayRestService.testProxy();
		logger.debug("Registering done");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


}
