package it.noi.edisplay.controller;

import it.noi.edisplay.dto.ConnectionDto;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.Location;
import it.noi.edisplay.repositories.ConnectionRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.LocationRepository;
import it.noi.edisplay.services.EDisplayRestService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


/**
 * Controller class to create API for CRUD operations on Connections
 */
@RestController
@RequestMapping("/connection")
public class ConnectionController {

	@Autowired
	ConnectionRepository connectionRepository;

	@Autowired
	DisplayRepository displayRepository;

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	ModelMapper modelMapper;
	Logger logger = LoggerFactory.getLogger(ConnectionController.class);
	@Autowired
	private EDisplayRestService eDisplayRestService;

	@RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<ConnectionDto> get(@RequestParam String uuid) {
		Connection connectionByUuid = connectionRepository.findByUuid(uuid);

		if (connectionByUuid == null) {
			logger.debug("Connection with uuid: " + uuid + " not found.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		ConnectionDto connectionDto = modelMapper.map(connectionByUuid, ConnectionDto.class);
		connectionDto.setLongitude(connectionByUuid.getCoordinates().getX());
		connectionDto.setLatitude(connectionByUuid.getCoordinates().getY());
		logger.debug("Get connection with uuid: " + uuid);
		return new ResponseEntity<ConnectionDto>(connectionDto, HttpStatus.OK);
	}

	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public ResponseEntity<ArrayList> getAllConnections() {
		ArrayList<Connection> list = modelMapper.map(connectionRepository.findAll(), ArrayList.class);
		ArrayList<ConnectionDto> dtoList = new ArrayList<>();
		for (Connection connection : list) {
			ConnectionDto connectionDto = modelMapper.map(connection, ConnectionDto.class);
			connectionDto.setLongitude(connection.getCoordinates().getX());
			connectionDto.setLatitude(connection.getCoordinates().getY());
			connectionDto.setConnected(connection.getConnected());
			connectionDto.setMac(connection.getMac());
			dtoList.add(connectionDto);
		}
		logger.debug("All connections requested");
		return new ResponseEntity<>(dtoList, HttpStatus.OK);
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<ConnectionDto> createConnection(@RequestBody ConnectionDto connectionDto) {
		Display display = displayRepository.findByUuid(connectionDto.getDisplayUuid());
		Location location = locationRepository.findByUuid(connectionDto.getLocationUuid());

		if (display == null || location == null) {
			logger.debug("Creation of connection failed.");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		}

		Connection connection = new Connection(display, location, new Point(connectionDto.getLongitude(), connectionDto.getLatitude()), connectionDto.getNetworkAddress());

		StateDto currentState = eDisplayRestService.getCurrentState(connection);
		if (currentState.getErrorMessage() != null) {
			connection.setConnected(false);
			logger.debug("Trying to connect to physical display failed with error: " + currentState.getErrorMessage());
		} else {
			connection.setConnected(true);
			connection.setMac(currentState.getMac());
		}
		Connection savedConnection = connectionRepository.save(connection);
		logger.debug("Connection with uuid:" + savedConnection.getUuid() + " created.");


		return new ResponseEntity<>(modelMapper.map(savedConnection, ConnectionDto.class), HttpStatus.OK);
	}

	@RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
	public ResponseEntity deleteConnection(@PathVariable("uuid") String uuid) {
		Connection connection = connectionRepository.findByUuid(uuid);

		if (connection == null) {
			logger.debug("Delete connection with uuid:" + uuid + " failed.");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		connectionRepository.delete(connection);
		logger.debug("Deleted connection with uuid:" + uuid);
		return new ResponseEntity(HttpStatus.OK);
	}

	@RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
	public ResponseEntity updateConnection(@RequestBody ConnectionDto connectionDto) {
		Connection connection = connectionRepository.findByUuid(connectionDto.getUuid());
		Display display = displayRepository.findByUuid(connectionDto.getDisplayUuid());

		//check if display has already other connection and delete it
		Connection connectionDisplay = connectionRepository.findByDisplay(display);
		if (connectionDisplay != null && !connectionDisplay.getUuid().equals(connection.getUuid())){
			logger.debug("Deleted existing connection with uuid:" + connectionDisplay.getUuid());
			connectionRepository.delete(connectionDisplay);
		}

		Location location = locationRepository.findByUuid(connectionDto.getLocationUuid());
		if (location == null) {
			logger.debug("Update connection with uuid:" + connectionDto.getUuid() + " failed.");
			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}

		connection.setLocation(location);
		connection.setConnected(true);
		connection.setMac(connectionDisplay != null ? connectionDisplay.getMac() : null);
		connection.setDisplay(display);
		connection.setNetworkAddress(connectionDto.getNetworkAddress()); //TODO check if address is correct and reachable
		connection.setCoordinates(new Point(connectionDto.getLongitude(), connectionDto.getLatitude()));

		connectionRepository.save(connection);

		logger.debug("Updated connection with uuid:" + connection.getUuid());
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}


}
