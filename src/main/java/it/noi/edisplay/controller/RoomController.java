// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.noi.edisplay.components.NOIDataLoader;
import it.noi.edisplay.dto.NOIPlaceData;
import it.noi.edisplay.dto.RoomDto;


/**
 * Controller class to create API for CRUD operations on NOI Rooms
 */
@RestController
@RequestMapping("/NOI-Place")
public class RoomController {
	
	@Autowired
	private NOIDataLoader noiDataLoader;
	
	Logger logger = LoggerFactory.getLogger(RoomController.class);
	
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public ResponseEntity<ArrayList<RoomDto>> getAllRooms() {
		ArrayList<RoomDto> roomList = new ArrayList<>();
		
		List<NOIPlaceData> places = noiDataLoader.getNOIPlaces();
		for (NOIPlaceData place : places) {
			RoomDto room = new RoomDto();
			room.setCode(place.getScode());
			room.setName(place.getName());
			room.setLabel(place.getRoomLabel());
			roomList.add(room);
		}

		logger.debug("All rooms requested");
		return new ResponseEntity<>(roomList, HttpStatus.OK);
	}
}