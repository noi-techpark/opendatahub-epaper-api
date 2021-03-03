package it.noi.edisplay.components;


import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.repositories.*;
import it.noi.edisplay.services.EDisplayRestService;
import it.noi.edisplay.services.OpenDataRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class EventsScheduler {

	private static final Logger logger = LoggerFactory.getLogger(EventsScheduler.class);


	@Value("${cron.enabled}")
	private Boolean enabled;

	@Autowired
	private OpenDataRestService openDataRestService;

	@Autowired
	private DisplayRepository displayRepository;

	@Autowired
	private TemplateRepository templateRepository;


	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private EDisplayRestService eDisplayRestService;

	private ArrayList<EventDto> events;

	// saves which event is visible on every used display, so it can be checked if event is already on display
	private HashMap<String, String> displayUuidToEventMapping = new HashMap<>();


	@Scheduled(cron = "${cron.opendata.events}")
	public void loadNoiTodayEvents() {
		if (enabled) {
			logger.debug("Loading Events from OpenDataHub START");

			events = openDataRestService.getEvents();

			logger.debug("Loaded " + openDataRestService.getEvents().size() + " events");
			logger.debug("Loading Events from OpenDataHub DONE");
		}
	}


	@Scheduled(cron = "${cron.opendata.displays}")
	public void updateDisplays() throws IOException {
		if (enabled) {
			logger.debug("Send events to display START");

			long currentTime = System.currentTimeMillis();

			//removes events that are finished or will finish in the next 5 minutes
			events.removeIf(eventDto -> eventDto.getRoomEndDateUTC() - 300000 < currentTime);

			ArrayList<String> checkedDisplays = new ArrayList<>();

			for (EventDto event : events) {
				String roomName = event.getSpaceDesc().replace("NOI ", "") + " Display";


				if (roomName.contains("+")) {
					String roomNumbers = roomName.replaceAll("\\D+", ""); //removes all non digits
					// assuming that rooms are Seminar rooms, since no other rooms have this kind of notation
					for (char roomNumber : roomNumbers.toCharArray()) {
						logger.debug("EVENT_SCHEDULER: Event with multiple rooms found: Seminar " + roomNumber);
						String seminarRoomName = "Seminar " + roomNumber + " Display";
						Display display = displayRepository.findByName(seminarRoomName); //needs to be optimized, if name changes it doesn't work anymore


						if (display != null) {
							if (!checkedDisplays.contains(display.getUuid())) {
								//check display/event mapping: if display not in list or event different, update event
								if (!displayUuidToEventMapping.containsKey(display.getUuid()) || !displayUuidToEventMapping.get(display.getUuid()).equals(event.getEventDescriptionEN())) {
									//update display
									logger.debug("EVENT_SCHEDULER: Event updated to " + event.getEventDescriptionEN() + " for display " + seminarRoomName);
									display.setTemplate(templateRepository.findByName(DefaultDataLoader.EVENT_TEMPLATE_NAME));
									Display savedDisplay = displayRepository.save(display);
									Connection connection = connectionRepository.findByDisplay(savedDisplay);
									displayUuidToEventMapping.put(display.getUuid(), event.getEventDescriptionEN());
									logger.debug("EVENT_SCHEDULER: Send image");
									eDisplayRestService.sendImageToDisplayAsync(connection, false);
								}
								checkedDisplays.add(display.getUuid());
							}
						}
					}
				} else {
					Display display = displayRepository.findByName(roomName); //needs to be optimized, if name changes it doesn't work anymore
					if (display != null) {
						if (!checkedDisplays.contains(display.getUuid())) {

							if (!displayUuidToEventMapping.containsKey(display.getUuid()) || !displayUuidToEventMapping.get(display.getUuid()).equals(event.getEventDescriptionEN())) {
								logger.debug("EVENT_SCHEDULER: Event updated to " + event.getEventDescriptionEN() + " for display " + roomName);
								display.setTemplate(templateRepository.findByName(DefaultDataLoader.EVENT_TEMPLATE_NAME));
								Display savedDisplay = displayRepository.save(display);
								Connection connection = connectionRepository.findByDisplay(savedDisplay);
								displayUuidToEventMapping.put(display.getUuid(), event.getEventDescriptionEN());
								logger.debug("EVENT_SCHEDULER: Send image");
								eDisplayRestService.sendImageToDisplayAsync(connection, false);
							}
							checkedDisplays.add(display.getUuid());
						}
					}
				}

			}

			logger.debug("Send events to display END");
		}
	}

	@PostConstruct
	private void postContrsuct() {
		loadNoiTodayEvents();
	}

}
