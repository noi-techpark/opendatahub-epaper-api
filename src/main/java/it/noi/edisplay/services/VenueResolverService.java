// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.services;


import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.dto.NOIPlaceData;
import it.noi.edisplay.dto.NOIPlaceDto;
import it.noi.edisplay.dto.support.RoomDetailDto;
import it.noi.edisplay.dto.support.VenueDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;

@Service
public class VenueResolverService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, String> roomIdToName = new HashMap<>();

    @Value("${open.data.venues.url}")
	private String venuesUrl;

    @PostConstruct
    public void loadVenue() {
        try {
            VenueDto venue = restTemplate.getForObject(venuesUrl, VenueDto.class);

            if (venue != null && venue.getRoomDetails() != null) {
                venue.getRoomDetails().forEach(room ->
                    roomIdToName.put(room.getId(), room.getShortname())
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // If it contains only one room
    public String resolve(String roomId) {
        return roomIdToName.getOrDefault(roomId, roomId);
    }

    // If it contains more rooms
    public List<String> resolve(List<String> roomIds) {
        if (roomIds == null) {
            return Collections.emptyList();
        }

        return roomIds.stream()
                .map(this::resolve)
                .collect(Collectors.toList());
    }

    
public List<String> normalizeUrns(List<String> venueRoomDetailsIds) {
        if (venueRoomDetailsIds == null) {
            return Collections.emptyList();
        }

        return venueRoomDetailsIds.stream()
            // URN -> shortname (A1-1.03, NOI A1-1.03, ecc.)
            .map(this::resolve)
            // final normalize 
            .map(this::normalizeRoom)
            .distinct()
            .collect(Collectors.toList());
    }

    private String normalizeRoom(String room) {
        if (room == null) return "";

        return room
            .replace("NOI", "");
    }

}

