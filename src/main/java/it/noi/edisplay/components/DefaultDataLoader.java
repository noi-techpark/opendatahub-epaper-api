package it.noi.edisplay.components;

import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.model.*;
import it.noi.edisplay.repositories.*;
import it.noi.edisplay.services.EDisplayRestService;
import it.noi.edisplay.services.OpenDataRestService;
import it.noi.edisplay.utils.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


@Component
public class DefaultDataLoader {

	public static final String EVENT_TEMPLATE_NAME = "NOI Template";
	private Logger logger = LoggerFactory.getLogger(DefaultDataLoader.class);
	@Value("${cron.enabled}")
	private Boolean enabled;

	@Autowired
	private OpenDataRestService openDataRestService;

	@Autowired
	private EDisplayRestService eDisplayRestService;

	@Autowired
	private DisplayRepository displayRepository;

	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private ImageUtil imageUtil;


	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private ResolutionRepository resolutionRepository;

	@Autowired
	private LocationRepository locationRepository;


	@PostConstruct
	public void onStartUp() throws IOException {

		if (templateRepository.findAll().size() == 0) {

			Template officeTemplate = new Template();
			Template meetingRoomTemplate = new Template();
			Template freeSoftwareLabTemplate = new Template();
			Template noiTemplate = new Template();


			officeTemplate.setName("Office");
			meetingRoomTemplate.setName("Meeting Room");
			freeSoftwareLabTemplate.setName("Free Software Lab");
			noiTemplate.setName(EVENT_TEMPLATE_NAME);

			officeTemplate.setImage(imageUtil.convertToMonochrome(ImageIO.read(new ClassPathResource("/default-templates/max-mustermann.png").getInputStream())));
			meetingRoomTemplate.setImage(imageUtil.convertToMonochrome(ImageIO.read(new ClassPathResource("/default-templates/meeting-room.png").getInputStream())));
			freeSoftwareLabTemplate.setImage(imageUtil.convertToMonochrome(ImageIO.read(new ClassPathResource("/default-templates/free-software-lab.png").getInputStream())));
			noiTemplate.setImage(imageUtil.convertToMonochrome(ImageIO.read(new ClassPathResource("/default-templates/noi.png").getInputStream())));


			templateRepository.save(officeTemplate);
			templateRepository.save(meetingRoomTemplate);
			templateRepository.save(noiTemplate);
			templateRepository.saveAndFlush(freeSoftwareLabTemplate);

		}

		if (resolutionRepository.findAll().size() == 0) {
			Resolution resolution = new Resolution();
			resolution.setWidth(640);
			resolution.setHeight(384);
			resolutionRepository.saveAndFlush(resolution);
		}

		if (locationRepository.findAll().size() == 0) {
			Location officeLocation = new Location();
			Location meetingRoomLocation = new Location();
			Location freeSoftwareLabLocation = new Location();


			officeLocation.setName("Office");
			meetingRoomLocation.setName("Meeting Room");
			freeSoftwareLabLocation.setName("Free Software Lab Default");

			locationRepository.save(officeLocation);
			locationRepository.save(meetingRoomLocation);
			locationRepository.saveAndFlush(freeSoftwareLabLocation);
		}


		if (enabled) {
			ArrayList<String> eventLocations = openDataRestService.getEventLocations();

			for (String eventLocation : eventLocations) {
				Location byName = locationRepository.findByName(eventLocation);
				if (byName == null) {

					Location location = new Location();
					location.setName(eventLocation);
					Location savedLocation = locationRepository.save(location);

					Display display = new Display();
					display.setName(eventLocation + " Display");
					display.setTemplate(templateRepository.findByName(EVENT_TEMPLATE_NAME));

					if (resolutionRepository.findAll().size() == 0) {
						Resolution resolution = new Resolution();
						resolution.setWidth(640);
						resolution.setHeight(384);
						display.setResolution(resolutionRepository.saveAndFlush(resolution));
					} else
						display.setResolution(resolutionRepository.findByWidthAndHeight(640, 384));
					display.setBatteryPercentage(new Random().nextInt(100));
					Display savedDisplay = displayRepository.save(display);

					Connection connection = new Connection();
					connection.setNetworkAddress("Please enter IP Address");
					connection.setCoordinates(new Point(0, 0));
					connection.setDisplay(savedDisplay);
					connection.setLocation(savedLocation);
					connection.setConnected(false);

					connectionRepository.save(connection);
				}
			}
			setNextEventOnDisplay();
		}
	}

	public void setNextEventOnDisplay() throws IOException {
		if (enabled) {
			ArrayList<EventDto> events = openDataRestService.getEvents();


			long currentTime = System.currentTimeMillis();
//			long currentTime = 1585994400000L; //4 april 2020 12:00
//			long currentTime = 1585994400000L; //4 april 2020 12:00
//			long currentTime = 1590044328000L; //21 MAY 2020 08:58
//			long currentTime = 1590060480000L; //21 MAY 2020 13:28
//			long currentTime = 1587212760000L; //18 april 2020 14:26

			//removes events that are finished or will finish in the next 5 minutes
			events.removeIf(eventDto -> eventDto.getRoomEndDateUTC() - 300000 < currentTime);

			// saves locations that already where the image has already been created, to prevent overwriting
			ArrayList<String> checkedLocations = new ArrayList<>();

			for (EventDto event : events) {


				String roomName = event.getSpaceDesc().replace("NOI ", "") + " Display";

				if (roomName.contains("+")) {
					String roomNumbers = roomName.replaceAll("\\D+", ""); //removes all non digits
					// assuming that rooms are Seminar rooms, since no other rooms have this kind of notation
					for (char roomNumber : roomNumbers.toCharArray()) {
						logger.debug("Default Data Loader: Event with multiple rooms found: Seminar " + roomNumber);
						String seminarRoomName = "Seminar " + roomNumber;
						String seminarDisplayName = seminarRoomName + " Display";
						if (!checkedLocations.contains(seminarDisplayName)) {
							Display display = displayRepository.findByName(seminarDisplayName); //needs to be optimized, if name changes it doesn't work anymore
							if (display != null) {
								display.setTemplate(templateRepository.findByName(EVENT_TEMPLATE_NAME));
								Display savedDisplay = displayRepository.save(display);
								Connection connection = connectionRepository.findByDisplay(savedDisplay);
								logger.debug("Default Data Loader: Send image multiple " + seminarRoomName);
								eDisplayRestService.sendImageToDisplayAsync(connection, false);
							}
							checkedLocations.add(seminarDisplayName);
						}
					}
				} else if (!checkedLocations.contains(roomName)) {
					Display display = displayRepository.findByName(roomName); //needs to be optimized, if name changes it doesn't work anymore
					if (display != null) {
						display.setTemplate(templateRepository.findByName(EVENT_TEMPLATE_NAME));
						Display savedDisplay = displayRepository.save(display);
						Connection connection = connectionRepository.findByDisplay(savedDisplay);
						logger.debug("Default Data Loader: Send image " + roomName);
						eDisplayRestService.sendImageToDisplayAsync(connection, false);
					}
					checkedLocations.add(roomName);
				}
			}
		}
	}
}



