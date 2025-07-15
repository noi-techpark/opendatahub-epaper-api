// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.components;

import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.dto.NOIPlaceData;
import it.noi.edisplay.dto.ScheduledContentDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.ScheduledContent;
import it.noi.edisplay.services.OpenDataRestService;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads and provides access to NOI events and places.
 */
@Component
public class NOIDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(NOIDataLoader.class);

    @Value("${cron.enabled}")
    private boolean enabled;

    private final OpenDataRestService openDataRestService;
    
    private final ModelMapper modelMapper;

    private List<EventDto> events = Collections.emptyList();

    private List<NOIPlaceData> places = Collections.emptyList();

    public NOIDataLoader(OpenDataRestService openDataRestService, ModelMapper modelMapper) {
        this.openDataRestService = openDataRestService;
        this.modelMapper = modelMapper;
    }

    @PostConstruct
    private void postConstruct() {
        loadNoiTodayEvents();
        loadNoiPlaces();
    }


    @Scheduled(cron = "${cron.opendata.events}")
    public void loadNoiTodayEvents() {
        if (!enabled) return;

        logger.info("Loading Events from OpenDataHub ...");
        try {
            List<EventDto> loadedEventDtos = openDataRestService.getEvents();
            if (loadedEventDtos != null) {
                events = loadedEventDtos;
            } else {
                events = Collections.emptyList();
            }
            logger.info("Loaded {} events!", events.size());
        } catch (Exception e) {
            logger.error("Failed to load events from OpenDataHub", e);
            events = Collections.emptyList();
        }
    }

    @Scheduled(cron = "${cron.opendata.locations}")
    public void loadNoiPlaces() {
        if (!enabled) return;

        logger.debug("Loading Places from OpenDataHub ...");
        try {
            List<NOIPlaceData> loadedPlaces = openDataRestService.getNOIPlaces();
            if (loadedPlaces != null) {
                places = loadedPlaces;
            } else {
                places = Collections.emptyList();
            }
            logger.debug("Loaded {} places!", places.size());
        } catch (Exception e) {
            logger.error("Failed to load places from OpenDataHub", e);
            places = Collections.emptyList();
        }
    }

    public List<EventDto> getNOIDisplayEvents(Display display) {

        if (display == null || display.getRoomCodes() == null || places == null || events == null) {
            return Collections.emptyList();
        }

        List<EventDto> noiEvents = new ArrayList<>();

        for (String roomCode : display.getRoomCodes()) {
            NOIPlaceData room = findRoomByCode(roomCode);
            if (room != null) {
                String normalizedRoomName = normalizeRoomName(room.getTodaynoibzit());
                noiEvents.addAll(events.stream()
                    .filter(event -> event.getSpaceDescList() != null && event.getSpaceDescList().contains(normalizedRoomName))
                    .collect(Collectors.toList()));
            }
        }
        return noiEvents;
    }

    public Map<String, List<EventDto>> getNOIDisplayEventsByRoom(Display display) {

        // use LinkedHashMap to have sorted map
        Map<String, List<EventDto>> noiEventsByRoom = new LinkedHashMap<>();
        if (display == null || display.getRoomCodes() == null || places == null || events == null) {
            return noiEventsByRoom;
        }

        for (String roomCode : display.getRoomCodes()) {
            NOIPlaceData room = findRoomByCode(roomCode);
            if (room != null) {
                String normalizedRoomName = normalizeRoomName(room.getTodaynoibzit());
                List<EventDto> roomEventDtos = events.stream()
                    .filter(event -> event.getSpaceDescList() != null && event.getSpaceDescList().contains(normalizedRoomName))
                    .collect(Collectors.toList()); 

                noiEventsByRoom.put(normalizedRoomName, roomEventDtos);
            }
        }

        return noiEventsByRoom;
    }

    public List<ScheduledContentDto> getAllDisplayEvents(Display display) {
        if (display == null) {
            return Collections.emptyList();
        }

        List<ScheduledContentDto> scheduledContentDtos = new ArrayList<>();
        List<ScheduledContent> scheduledContents = display.getScheduledContent();

        if (scheduledContents != null) {
            for (ScheduledContent scheduledContent : scheduledContents) {
                scheduledContentDtos.add(modelMapper.map(scheduledContent, ScheduledContentDto.class));
            }
        }

        if (display.getRoomCodes() != null && places != null && events != null) {
            // Get correct NOI room by Room Code
            for (String roomCode : display.getRoomCodes()) {
                NOIPlaceData room = findRoomByCode(roomCode);
                if (room != null) {
                    // Filter events based on NOI room that the display is in
                    String normalizedRoomName = normalizeRoomName(room.getTodaynoibzit());
                    List<EventDto> noiEvents = events.stream()
                        .filter(event -> event.getSpaceDescList() != null && event.getSpaceDescList().contains(normalizedRoomName))
                        .collect(Collectors.toList());
                    
                    for (EventDto noiEventDto : noiEvents) {
                        ScheduledContentDto scheduledContentDto = scheduledContentDtos.stream()
                            .filter(item -> item.getEventId() != null && item.getEventId().equals(noiEventDto.getEventId()))
                            .findFirst()
                            .orElse(null);
                        
                        if (scheduledContentDto == null) {
                            scheduledContentDto = new ScheduledContentDto();
                            scheduledContentDto.setStartDate(toTimestamp(noiEventDto.getRoomStartDateUTC()));
                            scheduledContentDto.setEndDate(toTimestamp(noiEventDto.getRoomEndDateUTC()));
                            scheduledContentDto.setEventDescription(noiEventDto.getEventDescriptionEN());
                            scheduledContentDto.setEventId(noiEventDto.getEventId());
                            scheduledContentDto.setDisplayUuid(display.getUuid());
                            scheduledContentDto.setSpaceDesc(noiEventDto.getSpaceDesc());
                            scheduledContentDtos.add(scheduledContentDto);
                        }
                        scheduledContentDto.setOriginalStartDate(toTimestamp(noiEventDto.getRoomStartDateUTC()));
                        scheduledContentDto.setOriginalEndDate(toTimestamp(noiEventDto.getRoomEndDateUTC()));
                        scheduledContentDto.setOriginalEventDescription(noiEventDto.getEventDescriptionEN());
                    }
                }
            }
        }
        return scheduledContentDtos;
    }

    public List<NOIPlaceData> getNOIPlaces() {
        return places != null ? places : Collections.emptyList();
    }

    private NOIPlaceData findRoomByCode(String roomCode) {
        if (roomCode == null || places == null) return null;
        return places.stream()
            .filter(item -> roomCode.equals(item.getScode()))
            .findFirst()
            .orElse(null);
    }

    private String normalizeRoomName(String roomName) {
        if (roomName == null || roomName.isEmpty()) return "";
        return roomName.replace("NOI ", "");
    }

    private Timestamp toTimestamp(Long epochMillis) {
        return epochMillis != null ? new Timestamp(epochMillis) : null;
    }

}
