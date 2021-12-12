package it.noi.edisplay.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.noi.edisplay.dto.NOIPlaceData;
import it.noi.edisplay.dto.NOIPlaceDto;
import it.noi.edisplay.dto.RoomDto;
import it.noi.edisplay.services.OpenDataRestService;


/**
 * Controller class to create API for CRUD operations on Locations
 */
@RestController
@RequestMapping("/NOI-Place")
public class RoomController {
	
	@Autowired
	private OpenDataRestService openDataRestService;
	
	Logger logger = LoggerFactory.getLogger(RoomController.class);
	
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public ResponseEntity getAllRooms() {
		ArrayList<RoomDto> roomList = new ArrayList<>();
		
		NOIPlaceDto places = openDataRestService.getNOIPlaces();
		for (NOIPlaceData place : places.getData()) {
			RoomDto room = new RoomDto();
			room.setCode(place.getScode());
			room.setName(place.getName());
			roomList.add(room);
		}

		logger.debug("All rooms requested");
		return new ResponseEntity<>(roomList, HttpStatus.OK);
	}
}