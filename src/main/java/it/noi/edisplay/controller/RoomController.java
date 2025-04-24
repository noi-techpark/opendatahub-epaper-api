// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.noi.edisplay.dto.RoomDto;
import it.noi.edisplay.services.RoomService;

/**
 * Controller class to create API for CRUD operations on NOI Rooms
 */
@RestController
@RequestMapping("/NOI-Place")
public class RoomController {

    @Autowired
    private RoomService roomService;

    Logger logger = LoggerFactory.getLogger(RoomController.class);

    @GetMapping("/all")
    public ResponseEntity<ArrayList<RoomDto>> getAllRooms() {
        try {
            ArrayList<RoomDto> roomList = roomService.getAllRooms();
            logger.debug("Returning {} rooms", roomList.size());
            return new ResponseEntity<>(roomList, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching rooms: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}