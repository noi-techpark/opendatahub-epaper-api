package it.noi.edisplay.components;

import it.noi.edisplay.controller.DisplayController;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.repositories.ConnectionRepository;
import it.noi.edisplay.services.EDisplayRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DisplayHeartbeatScheduler {

	private Logger logger = LoggerFactory.getLogger(DisplayController.class);

	@Autowired
	private ConnectionRepository connectionRepository;

	@Autowired
	private EDisplayRestService eDisplayRestService;


	@Scheduled(cron = "${cron.heartbeat}")
	public void heartbeat() {
		for (Connection connection : connectionRepository.findAll()){
			StateDto state = eDisplayRestService.getCurrentState(connection);
			if (state.getErrorMessage() != null){
				connection.setConnected(false);
				connectionRepository.save(connection);
				logger.debug("Heartbeat: No connection with uuid : " + connection.getUuid() + " possible");
			}
		}
	}

}
