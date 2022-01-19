package it.noi.edisplay.components;

import it.noi.edisplay.dto.EventDto;
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
		    //TODO: implement content update for displays
		}
	}

	@PostConstruct
	private void postContrsuct() {
		loadNoiTodayEvents();
	}

}
