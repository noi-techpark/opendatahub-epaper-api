package it.noi.edisplay.services;

import it.noi.edisplay.dto.ImageDto;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.utils.ImageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class EDisplayRestService {


	private final RestTemplate restTemplate;

	@Value("${proxy.remote}")
	private Boolean remote;

	@Value("${proxy.url}")
	private String proxyIpAddress;

	public EDisplayRestService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	@Async
	public CompletableFuture<StateDto> sendImageToDisplayAsync(Connection connection, boolean inverted) throws IOException {
		ResponseEntity<String> stringResponseEntity;
		if (!remote) {
			final String uri = "http://" + connection.getNetworkAddress();
			String image = ImageUtil.getBinaryImage(connection.getDisplay().getImage(), inverted, connection.getDisplay().getResolution());
			stringResponseEntity = restTemplate.postForEntity(uri, image, String.class);
		} else {
			String image = ImageUtil.getBinaryImage(connection.getDisplay().getImage(), inverted, connection.getDisplay().getResolution());
			final String uri = "http://" + proxyIpAddress + "/send?ip=" + connection.getNetworkAddress();
			ImageDto imageDto = new ImageDto(image);
			stringResponseEntity = restTemplate.postForEntity(uri, imageDto, String.class);
		}
		StateDto stateDto = new StateDto(stringResponseEntity.getBody().replaceAll("\r\n", "").split(";"));
		return CompletableFuture.completedFuture(stateDto);
	}

	public StateDto sendImageToDisplay(Connection connection, boolean inverted) throws IOException {
		ResponseEntity<String> stringResponseEntity;
		StateDto stateDto;
		try {
			if (!remote) {
				final String uri = "http://" + connection.getNetworkAddress();
				String image = ImageUtil.getBinaryImage(connection.getDisplay().getImage(), inverted, connection.getDisplay().getResolution());
				stringResponseEntity = restTemplate.postForEntity(uri, image, String.class);
			} else {
				String image = ImageUtil.getBinaryImage(connection.getDisplay().getImage(), inverted, connection.getDisplay().getResolution());
				final String uri = "http://" + proxyIpAddress + "/send?ip=" + connection.getNetworkAddress();
				ImageDto imageDto = new ImageDto(image);
				stringResponseEntity = restTemplate.postForEntity(uri, imageDto, String.class);
			}
			stateDto = new StateDto(stringResponseEntity.getBody().replaceAll("\r\n", "").split(";"));
		} catch (ResourceAccessException e) {
			String[] states = {"0", "0", "0", "Unknown Host", ""};
			stateDto = new StateDto(states);
		}
		return stateDto;
	}

	public StateDto clearDisplay(Connection connection) {
		ResponseEntity<String> stringResponseEntity;
		StateDto stateDto;
		try {
			if (!remote) {
				final String uri = "http://" + connection.getNetworkAddress();
				stringResponseEntity = restTemplate.postForEntity(uri, "2", String.class);
			} else {
				final String uri = "http://" + proxyIpAddress + "/clear?ip=" + connection.getNetworkAddress();
				stringResponseEntity = restTemplate.postForEntity(uri, null, String.class);
			}
			stateDto = new StateDto(stringResponseEntity.getBody().replaceAll("\r\n", "").split(";"));
		} catch (ResourceAccessException e) {
			String[] states = {"0", "0", "0", "Unknown Host", ""};
			stateDto = new StateDto(states);
		}
		return stateDto;
	}

	public StateDto getCurrentState(Connection connection) {
		StateDto stateDto;
		try {
			if (!remote) {
				final String uri = "http://" + connection.getNetworkAddress();
				ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(uri, "3", String.class);//2 means clear display
				String[] states = stringResponseEntity.getBody().replaceAll("\r\n", "").split(";");
				stateDto = new StateDto(states);

			} else {
				final String uri = "http://" + proxyIpAddress + "/state?ip=" + connection.getNetworkAddress();
				ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(uri, null, String.class);
				stateDto = new StateDto(stringResponseEntity.getBody().replaceAll("\r\n", "").split(";"));
			}
		} catch (ResourceAccessException e) {
			String[] states = {"0", "0", "0", "Unknown Host", ""};
			stateDto = new StateDto(states);
		}
		return stateDto;
	}
}
