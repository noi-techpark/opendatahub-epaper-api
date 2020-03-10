package it.noi.edisplay.services;

import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.utils.ImageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class EDisplayRestService {


	private final RestTemplate restTemplate;

	@Value("${remote}")
	private Boolean remote;

	@Value("${proxyIpAddress}")
	private String proxyIpAddress;

	public EDisplayRestService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();

	}

	public void sendImageToDisplay(Display display, Connection connection, boolean inverted) throws IOException {
		if (!remote) {
			final String uri = "http://" + connection.getNetworkAddress();
			String image = ImageUtil.getBinaryImage(display.getImage(), inverted, display.getResolution());
			restTemplate.postForLocation(uri, image);
		} else {
			String image = ImageUtil.getBinaryImage(display.getImage(), inverted, display.getResolution());
			final String uri = "http://" + proxyIpAddress + "/send?ip="+connection.getNetworkAddress()+"&image="+image;
			restTemplate.postForLocation(uri, null);
		}
	}

	public void clearDisplay(Connection connection) {
		if(!remote) {
			final String uri = "http://" + connection.getNetworkAddress();
			restTemplate.postForLocation(uri, "2"); //2 means clear display
		} else{
			final String uri = "http://" + proxyIpAddress + "/clear?ip="+connection.getNetworkAddress();
			restTemplate.postForLocation(uri, null);
		}
	}

	public StateDto getCurrentState(Connection connection) {
		if(!remote) {
			final String uri = "http://" + connection.getNetworkAddress();
			ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(uri, "3", String.class);//2 means clear display
			String[] states = stringResponseEntity.getBody().split(";");
			return new StateDto(states);
		}else{
			final String uri = "http://" + proxyIpAddress + "/state?ip=" + connection.getNetworkAddress();
			ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(uri,null, String.class);//2 means clear display
			String[] states = stringResponseEntity.getBody().split(";");
			return new StateDto(states);
		}
	}
}
