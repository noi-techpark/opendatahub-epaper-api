package it.noi.edisplay.components;


import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.model.Location;
import it.noi.edisplay.repositories.ConnectionRepository;
import it.noi.edisplay.repositories.DisplayRepository;
import it.noi.edisplay.repositories.LocationRepository;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.services.EDisplayRestService;
import it.noi.edisplay.services.OpenDataRestService;
import it.noi.edisplay.utils.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

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
	private ConnectionRepository connectionRepository;

	@Autowired
	private ResolutionRepository resolutionRepository;

	@Autowired
	private EDisplayRestService eDisplayRestService;

	private ArrayList<EventDto> events;

	private ArrayList<String> eventLocations;


	//	@Scheduled(cron = "${cron.opendata}")
//	@Scheduled(fixedRate = 200000)
	public void loadNoiTodayEvents() {
		if (enabled) {
			logger.debug("Loading Events from OpenDataHub START");

			events = openDataRestService.getEvents();

			logger.debug("Loading Events from OpenDataHub DONE");
		}
	}


	@Scheduled(fixedRate = 20000)
	public void updateDisplay() throws IOException {
		if (enabled) {
			logger.debug("Send events to display START");

//			long currentTime = new Date().getTime();
//			long currentTime = 1585994400000L; //4 april 2020 12:00
//			long currentTime = 1586003160000L; //4 april 2020 14:26
			long currentTime = 1587212760000L; //18 april 2020 14:26

			//removes events that are finished or will finish in the next 5 minutes
			events.removeIf(eventDto -> eventDto.getEventEndDateUTC() - 300000 < currentTime);

			for (EventDto event : events){
				if(event.getEventStartDateUTC() - 300000 < currentTime && event.getEventStartDateUTC() > currentTime){ //events will start next 5 minutes
					Display display = displayRepository.findByName(event.getSpaceDesc() + " Display"); //needs to be optimized, if name changes it doesn't work anymore

					display.setImage(ImageUtil.getImageForEvent(event));

					Display savedDisplay = displayRepository.save(display);
					Connection connection = connectionRepository.findByDisplay(savedDisplay);
					eDisplayRestService.sendImageToDisplay(connection, false);
				}
			}


			logger.debug("Send events to display END");
		}
	}

	@PostConstruct
	private void postConstruct() {
		if (enabled) {
			loadNoiTodayEvents();

			ArrayList<String> eventLocations = openDataRestService.getEventLocations();

			for (String eventLocation : eventLocations) {
				Location byName = locationRepository.findByName(eventLocation);
				if (byName == null) {

					Location location = new Location();
					location.setName(eventLocation);
					Location savedLocation = locationRepository.save(location);

					Display display = new Display();
					display.setName(eventLocation + " Display");
					display.setResolution(resolutionRepository.findByWidthAndHeight(640, 384));
					display.setBatteryPercentage(new Random().nextInt(100));
//				display.setImage();
					Display savedDisplay = displayRepository.save(display);

					Connection connection = new Connection();
					connection.setNetworkAddress("Please enter IP Address");
					connection.setCoordinates(new Point(0, 0));
					connection.setDisplay(savedDisplay);
					connection.setLocation(savedLocation);

					connectionRepository.save(connection);
				}
			}
		}
	}
}
