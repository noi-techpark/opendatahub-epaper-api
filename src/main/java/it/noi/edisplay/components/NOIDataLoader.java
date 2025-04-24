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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NOIDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(NOIDataLoader.class);

    @Value("${cron.enabled}")
    private boolean enabled;

    @Autowired
    private OpenDataRestService openDataRestService;

    private List<EventDto> events;

    private List<NOIPlaceData> places;

    @Autowired
    ModelMapper modelMapper;

    // @Scheduled(cron = "${cron.opendata.events}")
    public void loadNoiTodayEvents() {
        if (enabled) {
            logger.debug("Loading Events from OpenDataHub START");

            events = openDataRestService.getEvents();

            logger.debug("Loaded " + events.size() + " events");
            logger.debug("Loading Events from OpenDataHub DONE");
        }
    }

    // @Scheduled(cron = "${cron.opendata.locations}")
    public void loadNoiPlaces() {
        if (enabled) {
            logger.debug("Loading Places from OpenDataHub START");

            places = openDataRestService.getNOIPlaces();

            logger.debug("Loaded " + places.size() + " places");
            logger.debug("Loading Places from OpenDataHub DONE");
        }
    }

    public List<EventDto> getNOIDisplayEvents(Display display) {
        List<EventDto> noiEvents = new ArrayList<>();

        if (display.getRoomCodes() != null) {
            // Get correct NOI room by Room Code
            for (String roomCode : display.getRoomCodes()) {

                NOIPlaceData room = places.stream().filter(item -> item.getScode().equals(roomCode))
                        .findFirst().orElse(null);
                if (room != null) {
                    // Filter events based on NOI room that the display is in
                    noiEvents.addAll(events.stream().filter(
                            item -> item.getSpaceDescList()
                                    .contains(room.getTodaynoibzit() == null || room.getTodaynoibzit().isEmpty() ? ""
                                            : room.getTodaynoibzit().replace("NOI ", "")))
                            .collect(Collectors.toList()));
                }

            }
        }
        return noiEvents;
    }

    public Map<String, List<EventDto>> getNOIDisplayEventsByRoom(Display display) {

        // use LinkedHashMap to have sorted map
        Map<String, List<EventDto>> noiEvents = new LinkedHashMap<>();

        if (display.getRoomCodes() != null) {
            // Get correct NOI room by Room Code
            for (String roomCode : display.getRoomCodes()) {
                NOIPlaceData room = places.stream().filter(item -> item.getScode().equals(roomCode))
                        .findFirst().orElse(null);
                if (room != null) {
                    // Filter events based on NOI room that the display is in
                    String roomName = room.getTodaynoibzit() == null || room.getTodaynoibzit().isEmpty() ? ""
                            : room.getTodaynoibzit().replace("NOI ", "");
                    noiEvents.put(roomName, events.stream().filter(
                            item -> item.getSpaceDescList()
                                    .contains(roomName))
                            .collect(Collectors.toList()));
                }
            }
        }
        return noiEvents;
    }

    public List<ScheduledContentDto> getAllDisplayEvents(Display display) {
        List<ScheduledContent> scheduledContentList = display.getScheduledContent();

        ArrayList<ScheduledContentDto> dtoList = new ArrayList<>();

        // Add events that are in the eInk database
        for (ScheduledContent scheduledContent : scheduledContentList)
            dtoList.add(modelMapper.map(scheduledContent, ScheduledContentDto.class));

        if (display.getRoomCodes() != null) {
            for (String roomCode : display.getRoomCodes()) {

                // Get correct NOI room by Room Code
                NOIPlaceData room = places.stream().filter(item -> item.getScode().equals(roomCode))
                        .findFirst().orElse(null);
                if (room != null) {
                    // Filter events based on NOI room that the display is in
                    List<EventDto> noiEvents = events.stream()
                            .filter(item -> item.getSpaceDescList()
                                    .contains(room.getTodaynoibzit() == null || room.getTodaynoibzit().isEmpty() ? ""
                                            : room.getTodaynoibzit().replace("NOI ", "")))
                            .collect(Collectors.toList());
                    for (EventDto noiEvent : noiEvents) {
                        // Look for modified NOI events that are saved in the eInk database
                        ScheduledContentDto scheduledContentDto = dtoList.stream().filter(
                                item -> item.getEventId() != null && item.getEventId().equals(noiEvent.getEventId()))
                                .findFirst().orElse(null);
                        // If NOI event is not present in the eInk database, create new DTO
                        if (scheduledContentDto == null) {
                            scheduledContentDto = new ScheduledContentDto();
                            scheduledContentDto.setStartDate(new Timestamp(noiEvent.getRoomStartDateUTC()));
                            scheduledContentDto.setEndDate(new Timestamp(noiEvent.getRoomEndDateUTC()));
                            scheduledContentDto.setEventDescription(noiEvent.getEventDescriptionEN());
                            scheduledContentDto.setEventId(noiEvent.getEventId());
                            scheduledContentDto.setDisplayUuid(display.getUuid());
                            scheduledContentDto.setSpaceDesc(noiEvent.getSpaceDesc());
                            dtoList.add(scheduledContentDto);
                        }

                        scheduledContentDto.setOriginalStartDate(new Timestamp(noiEvent.getRoomStartDateUTC()));
                        scheduledContentDto.setOriginalEndDate(new Timestamp(noiEvent.getRoomEndDateUTC()));
                        scheduledContentDto.setOriginalEventDescription(noiEvent.getEventDescriptionEN());
                    }
                }
            }
        }
        return dtoList;
    }

    public List<NOIPlaceData> getNOIPlaces() {
        return places;
    }

    @PostConstruct
    private void postConstruct() {
        loadNoiTodayEvents();
        loadNoiPlaces();
    }

}
