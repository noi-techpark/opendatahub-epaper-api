// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.noi.edisplay.components.NOIDataLoader;
import it.noi.edisplay.dto.NOIPlaceData;
import it.noi.edisplay.dto.RoomDto;

/**
 * Service class for NOI Rooms business logic.
 */
@Service
public class RoomService {

    @Autowired
    private NOIDataLoader noiDataLoader;

    Logger logger = LoggerFactory.getLogger(RoomService.class);

    public ArrayList<RoomDto> getAllRooms() {
        ArrayList<RoomDto> roomList = new ArrayList<>();

        if (noiDataLoader == null) {
            logger.error("NOIDataLoader is null");
            throw new IllegalStateException("NOIDataLoader is null");
        }

        logger.debug("About to fetch NOI places");
        List<NOIPlaceData> places = noiDataLoader.getNOIPlaces();

        if (places == null) {
            logger.error("getNOIPlaces() returned null");
            return roomList;
        }

        for (NOIPlaceData place : places) {
            if (place != null) {
                RoomDto room = new RoomDto();
                room.setCode(place.getScode());
                room.setName(place.getName());
                room.setLabel(place.getRoomLabel());
                roomList.add(room);
            }
        }

        return roomList;
    }
}