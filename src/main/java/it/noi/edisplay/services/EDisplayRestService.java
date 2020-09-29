package it.noi.edisplay.services;

import it.noi.edisplay.dto.ImageDto;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.utils.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class EDisplayRestService {


	private final RestTemplate restTemplate;

	@Value("${proxy.enabled}")
	private Boolean enabled;

	@Value("${proxy.url}")
	private String proxyIpAddress;


	@Autowired
	private ImageUtil imageUtil;

	public EDisplayRestService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	@Async
	public CompletableFuture<StateDto> sendImageToDisplayAsync(Connection connection, boolean inverted) throws IOException {
		StateDto stateDto;
		if (!enabled) {
			final String uri = "http://" + connection.getNetworkAddress();
			String image = imageUtil.getCodeFromImage(connection.getDisplay().getImage());
			stateDto = restTemplate.postForObject(uri, image, StateDto.class);
		} else {
			String image = imageUtil.getBinaryImage(connection.getDisplay().getImage(),false,connection.getDisplay().getResolution());
			final String uri = "http://" + proxyIpAddress + "/send?ip=" + connection.getNetworkAddress();
			ImageDto imageDto = new ImageDto(image);
			stateDto = restTemplate.postForObject(uri, imageDto, StateDto.class);
		}
		return CompletableFuture.completedFuture(stateDto);
	}

	public StateDto sendImageToDisplay(Connection connection, boolean inverted) throws IOException {
		ResponseEntity<String> stringResponseEntity;
		StateDto stateDto;
		try {
			if (!enabled) {
				final String uri = "http://" + connection.getNetworkAddress();
				String image = imageUtil.getCodeFromImage(connection.getDisplay().getImage());
				stateDto = restTemplate.postForObject(uri, image, StateDto.class);
			} else {
				String image = imageUtil.getBinaryImage(connection.getDisplay().getImage(),false,connection.getDisplay().getResolution());
				final String uri = "http://" + proxyIpAddress + "/send?ip=" + connection.getNetworkAddress();
				ImageDto imageDto = new ImageDto(image);
				stateDto = restTemplate.postForObject(uri, imageDto, StateDto.class);
			}
		} catch (ResourceAccessException e) {
			stateDto = new StateDto("Display not reachable");
		} catch (HttpServerErrorException e) {
			stateDto = new StateDto("Sending failed, please try again");
		}
		return stateDto;
	}

	public StateDto clearDisplay(Connection connection) {
		ResponseEntity<String> stringResponseEntity;
		StateDto stateDto;
		try {
			if (!enabled) {
				final String uri = "http://" + connection.getNetworkAddress();
				stateDto = restTemplate.postForObject(uri, "2", StateDto.class);
			} else {
				final String uri = "http://" + proxyIpAddress + "/clear?ip=" + connection.getNetworkAddress();
				stateDto = restTemplate.postForObject(uri, null, StateDto.class);
			}
		} catch (ResourceAccessException e) {
			stateDto = new StateDto("Display not reachable");
		}
		return stateDto;
	}

	public StateDto getCurrentState(Connection connection) {
		StateDto stateDto;
		try {
			if (!enabled) {
				final String uri = "http://" + connection.getNetworkAddress();
				stateDto = restTemplate.postForObject(uri, "3", StateDto.class);//2 means clear display

			} else {
				final String uri = "http://" + proxyIpAddress + "/state?ip=" + connection.getNetworkAddress();
				stateDto = restTemplate.postForObject(uri, null, StateDto.class);//2 means clear display
			}
		} catch (ResourceAccessException e) {
			stateDto = new StateDto("Display not reachable");
		}
		return stateDto;
	}
}
