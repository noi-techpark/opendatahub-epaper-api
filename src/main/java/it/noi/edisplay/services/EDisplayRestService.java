package it.noi.edisplay.services;

import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.utils.ImageUtil;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class EDisplayRestService {

	private final RestTemplate restTemplate;

	public EDisplayRestService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public void sendImageToDisplay(Display display, Connection connection, boolean inverted) throws IOException {
		final String uri = "http://" + connection.getNetworkAddress();
		String image = ImageUtil.getBinaryImage(display.getImage(), inverted, display.getResolution());
		restTemplate.postForLocation(uri, image);
	}

	public void clearDisplay(Connection connection) {
		final String uri = "http://" + connection.getNetworkAddress();
		restTemplate.postForLocation(uri, "2"); //2 means clear display
	}

	public StateDto getCurrentState(Connection connection) {
		final String uri = "http://" + connection.getNetworkAddress();
		ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(uri, "3", String.class);//2 means clear display
		String[] states = stringResponseEntity.getBody().split(";");
		return new StateDto(states);
	}
}
