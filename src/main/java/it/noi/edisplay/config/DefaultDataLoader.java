package it.noi.edisplay.config;

import it.noi.edisplay.model.Location;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.repositories.LocationRepository;
import it.noi.edisplay.repositories.ResolutionRepository;
import it.noi.edisplay.repositories.TemplateRepository;
import it.noi.edisplay.utils.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Component
public class DefaultDataLoader {

	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private ResolutionRepository resolutionRepository;

	@Autowired
	private LocationRepository locationRepository;


	//method invoked during the startup
	@PostConstruct
	public void loadTemplates() throws IOException {

		if (templateRepository.findAll().size() == 0) {

			Template officeTemplate = new Template();
			Template meetingRoomTemplate = new Template();
			Template freeSoftwareLabTemplate = new Template();


			officeTemplate.setName("Office");
			meetingRoomTemplate.setName("Meeting Room");
			freeSoftwareLabTemplate.setName("Free Software Lab");

			officeTemplate.setImage(ImageUtil.convertToMonochrome(ImageIO.read(new ClassPathResource("/default-templates/max-mustermann.png").getInputStream())));
			meetingRoomTemplate.setImage(ImageUtil.convertToMonochrome(ImageIO.read(new ClassPathResource("/default-templates/meeting-room.png").getInputStream())));
			freeSoftwareLabTemplate.setImage(ImageUtil.convertToMonochrome(ImageIO.read(new ClassPathResource("/default-templates/free-software-lab.png").getInputStream())));


			templateRepository.save(officeTemplate);
			templateRepository.save(meetingRoomTemplate);
			templateRepository.saveAndFlush(freeSoftwareLabTemplate);

		}

		if (resolutionRepository.findAll().size() == 0){

			Resolution resolution = new Resolution();
			resolution.setWidth(640);
			resolution.setHeight(384);


			resolutionRepository.saveAndFlush(resolution);
		}

		if (locationRepository.findAll().size() == 0){
			Location officeLocation = new Location();
			Location meetingRoomLocation = new Location();
			Location freeSoftwareLabLocation = new Location();


			officeLocation.setName("Office");
			meetingRoomLocation.setName("Meeting Room");
			freeSoftwareLabLocation.setName("Free Software Lab");

			locationRepository.save(officeLocation);
			locationRepository.save(meetingRoomLocation);
			locationRepository.saveAndFlush(freeSoftwareLabLocation);
		}

	}

	//method invoked during the shutdown
//    @PreDestroy
//    public void removeData() {

//    }
}
