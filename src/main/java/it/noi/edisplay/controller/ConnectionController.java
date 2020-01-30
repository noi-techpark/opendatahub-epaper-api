package it.noi.edisplay.controller;

import it.noi.edisplay.dto.ConnectionDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.Location;
import it.noi.edisplay.model.ProtocolType;
import it.noi.edisplay.repositories.ConnectionRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.LocationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

    @RequestMapping(value = "/get/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<ConnectionDto> get(@RequestParam String uuid) {
        Connection connectionByUuid = connectionRepository.findByUuid(uuid);

        if (connectionByUuid == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        ConnectionDto connectionDto = modelMapper.map(connectionByUuid, ConnectionDto.class);
        connectionDto.setLongitude(connectionByUuid.getCoordinates().getX());
        connectionDto.setLatitude(connectionByUuid.getCoordinates().getY());
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
            dtoList.add(connectionDto);
        }
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<ConnectionDto> createConnection(@RequestBody ConnectionDto connectionDto) {
        Display display = displayRepository.findByUuid(connectionDto.getDisplayUuid());
        Location location = locationRepository.findByUuid(connectionDto.getLocationUuid());

        if (display == null || location == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Connection connection = connectionRepository.save(new Connection(display, location, connectionDto.getName(), new Point(connectionDto.getLongitude(),connectionDto.getLatitude()), connectionDto.getNetworkAddress()));
        return new ResponseEntity<>(modelMapper.map(connection, ConnectionDto.class), HttpStatus.OK);
    }

    @RequestMapping(value = "/delete/{uuid}", method = RequestMethod.DELETE)
    public ResponseEntity deleteConnection(@PathVariable("uuid") String uuid) {
        Connection connection = connectionRepository.findByUuid(uuid);

        if (connection == null)
            return new ResponseEntity(HttpStatus.BAD_REQUEST);

        try {
            connectionRepository.delete(connection);
        } catch (EmptyResultDataAccessException ex) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = "application/json")
    public ResponseEntity updateConnection(@RequestBody ConnectionDto connectionDto) {
        Connection connection = connectionRepository.findByUuid(connectionDto.getUuid());
        Display display = displayRepository.findByUuid(connectionDto.getDisplayUuid());
        Location location = locationRepository.findByUuid(connectionDto.getLocationUuid());
        if (connection == null || display == null || location == null)
            return new ResponseEntity(HttpStatus.BAD_REQUEST);

        connection.setName(connectionDto.getName());
        connection.setLocation(location);
        connection.setDisplay(display);
        connection.setCoordinates(new Point(connectionDto.getLongitude(),connectionDto.getLatitude()));

        connectionRepository.save(connection);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }


}
