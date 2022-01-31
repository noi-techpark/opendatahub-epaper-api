package it.noi.edisplay.components;

import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.dto.NOIPlaceData;
import it.noi.edisplay.dto.ScheduledContentDto;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.Location;
import it.noi.edisplay.model.ScheduledContent;
import it.noi.edisplay.services.OpenDataRestService;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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

    @Scheduled(cron = "${cron.opendata.events}")
    public void loadNoiTodayEvents() {
        if (enabled) {
            logger.debug("Loading Events from OpenDataHub START");

            events = openDataRestService.getEvents();

            logger.debug("Loaded " + openDataRestService.getEvents().size() + " events");
            logger.debug("Loading Events from OpenDataHub DONE");
        }
    }

    @Scheduled(cron = "${cron.opendata.locations}")
    public void loadNoiPlaces() {
        if (enabled) {
            logger.debug("Loading Places from OpenDataHub START");

            places = openDataRestService.getNOIPlaces();

            logger.debug("Loaded " + openDataRestService.getEvents().size() + " places");
            logger.debug("Loading Places from OpenDataHub DONE");
        }
    }

    public List<EventDto> getNOIDisplayEvents(Display display) {
        List<EventDto> noiEvents = new ArrayList<>();
        Location displayLocation = display.getLocation();

        if (displayLocation != null && displayLocation.getRoomCode() != null) {
            // Get correct NOI room by Room Code
            NOIPlaceData room = places.stream().filter(item -> item.getScode().equals(displayLocation.getRoomCode()))
                    .findFirst().orElse(null);
            if (room != null) {
                // Filter events based on NOI room that the display is in
                noiEvents = events.stream().filter(item -> item.getSpaceDesc().equals(room.getTodaynoibzit()))
                        .collect(Collectors.toList());
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

        Location displayLocation = display.getLocation();

        if (displayLocation != null && displayLocation.getRoomCode() != null) {
            // Get correct NOI room by Room Code
            NOIPlaceData room = places.stream().filter(item -> item.getScode().equals(displayLocation.getRoomCode()))
                    .findFirst().orElse(null);
            if (room != null) {
                // Filter events based on NOI room that the display is in
                List<EventDto> noiEvents = events.stream()
                        .filter(item -> item.getSpaceDesc().equals(room.getTodaynoibzit()))
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
                        dtoList.add(scheduledContentDto);
                    }

                    scheduledContentDto.setOriginalStartDate(new Timestamp(noiEvent.getRoomStartDateUTC()));
                    scheduledContentDto.setOriginalEndDate(new Timestamp(noiEvent.getRoomEndDateUTC()));
                    scheduledContentDto.setOriginalEventDescription(noiEvent.getEventDescriptionEN());
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
