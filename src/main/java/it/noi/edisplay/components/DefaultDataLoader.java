package it.noi.edisplay.components;

import it.noi.edisplay.model.*;
import it.noi.edisplay.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class DefaultDataLoader {

	public static final String EVENT_TEMPLATE_NAME = "NOI Template";
	private Logger logger = LoggerFactory.getLogger(DefaultDataLoader.class);
	@Value("${cron.enabled}")
	private Boolean enabled;

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

			templateRepository.save(officeTemplate);
			templateRepository.save(meetingRoomTemplate);
			templateRepository.save(noiTemplate);
			templateRepository.saveAndFlush(freeSoftwareLabTemplate);

		}

		if (resolutionRepository.findAll().size() == 0) {
			Resolution resolution = new Resolution();
			resolution.setWidth(1440);
			resolution.setHeight(2560);
			resolution.setBitDepth(24);
			resolutionRepository.save(resolution);
	        Resolution resolutionSmall = new Resolution();
	        resolutionSmall.setWidth(1872);
	        resolutionSmall.setHeight(1404);
	        resolutionSmall.setBitDepth(4);
	        resolutionRepository.saveAndFlush(resolutionSmall);
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
	}
}



