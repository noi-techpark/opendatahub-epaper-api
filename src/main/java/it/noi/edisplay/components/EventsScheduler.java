package it.noi.edisplay.components;


import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.repositories.*;
import it.noi.edisplay.services.EDisplayRestService;
import it.noi.edisplay.services.OpenDataRestService;
import it.noi.edisplay.utils.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component
public class EventsScheduler {

	private static final Logger logger = LoggerFactory.getLogger(EventsScheduler.class);
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");


	@Value("${cron.enabled}")
	private Boolean enabled;

	@Autowired
	private OpenDataRestService openDataRestService;

	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	private DisplayRepository displayRepository;

	@Autowired
	private TemplateRepository templateRepository;


	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private ResolutionRepository resolutionRepository;

	@Autowired
	private EDisplayRestService eDisplayRestService;

	private ArrayList<EventDto> events;


	@Scheduled(cron = "${cron.opendata.events}")
//	@Scheduled(fixedDelay = 1000000)
	public void loadNoiTodayEvents() {
		if (enabled) {
			logger.debug("Loading Events from OpenDataHub START");

			events = openDataRestService.getEvents();

			logger.debug("Loaded " + openDataRestService.getEvents().size() + " events");
			logger.debug("Loading Events from OpenDataHub DONE");
		}
	}


	@Scheduled(cron = "${cron.opendata.displays}")
//	@Scheduled(fixedDelay = 1000000)
	public void updateDisplays() throws IOException {
		if (enabled) {
			logger.debug("Send events to display START");

			long currentTime = System.currentTimeMillis();
//			long currentTime = 1585994400000L; //4 april 2020 12:00
//			long currentTime = 1590044328000L; //21 MAY 2020 08:58
//			long currentTime = 1590060480000L; //21 MAY 2020 13:28
//			long currentTime = 1587212760000L; //18 april 2020 14:26

			//removes events that are finished or will finish in the next 5 minutes
			events.removeIf(eventDto -> eventDto.getEventEndDateUTC() - 300000 < currentTime);

			for (EventDto event : events) {
				if (event.getEventStartDateUTC() - 300000 < currentTime && event.getEventStartDateUTC() > currentTime) { //events will start next 5 minutes
					String roomName = event.getSpaceDesc().replace("NOI ", "") + " Display";

					if (roomName.contains("+")) {
						String roomNumbers = roomName.replaceAll("\\D+", ""); //removes all non digits
						// assuming that rooms are Seminar rooms, since no other rooms have this kind of notation
						for (char roomNumber : roomNumbers.toCharArray()) {
							logger.debug("EVENT_SCHEDULER: Event with multiple rooms found: Seminar " + roomNumber);
							String seminarRoomName = "Seminar " + roomNumber + " Display";
							Display display = displayRepository.findByName(seminarRoomName); //needs to be optimized, if name changes it doesn't work anymore
							if (display != null) {
								display.setImage(ImageUtil.getImageForEvent(event, templateRepository.findByName(DefaultDataLoader.EVENT_TEMPLATE_NAME).getImage()));
								Display savedDisplay = displayRepository.save(display);
								Connection connection = connectionRepository.findByDisplay(savedDisplay);
								logger.debug("EVENT_SCHEDULER: Send image");
								eDisplayRestService.sendImageToDisplay(connection, false);
								logger.debug("EVENT_SCHEDULER: Send image done");
							}
						}
					} else {
						Display display = displayRepository.findByName(roomName); //needs to be optimized, if name changes it doesn't work anymore
						if (display != null) {
							display.setImage(ImageUtil.getImageForEvent(event, templateRepository.findByName(DefaultDataLoader.EVENT_TEMPLATE_NAME).getImage()));
							Display savedDisplay = displayRepository.save(display);
							Connection connection = connectionRepository.findByDisplay(savedDisplay);
							eDisplayRestService.sendImageToDisplay(connection, false);
//						eDisplayRestService.sendImageToDisplayAsync(connection, false); TODO check if async or not
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
