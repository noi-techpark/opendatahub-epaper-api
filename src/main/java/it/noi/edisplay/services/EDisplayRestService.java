package it.noi.edisplay.services;

import it.noi.edisplay.dto.ImageDto;
import it.noi.edisplay.dto.StateDto;
import it.noi.edisplay.dto.WSImageDto;
import it.noi.edisplay.dto.WSRequestDto;
import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Template;
import it.noi.edisplay.utils.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Service
public class EDisplayRestService {

	// private final int HTTP_CONNECT_TIMEOUT = 1500000;
	// private final int HTTP_READ_TIMEOUT = 1000000;

	private RestTemplate restTemplate;

	@Value("${proxy.enabled}")
	private Boolean proxyEnabled;

	@Value("${proxy.url}")
	private String proxyIpAddress;

	@Value("${websocket.enabled}")
	private Boolean webSocketEnabled;

	@Autowired
	private SimpMessagingTemplate webSocket;

	@Autowired
	private ImageUtil imageUtil;

	public EDisplayRestService(RestTemplateBuilder restTemplateBuilder) {
//		restTemplateBuilder.setConnectTimeout(HTTP_CONNECT_TIMEOUT);
//		restTemplateBuilder.setReadTimeout(HTTP_READ_TIMEOUT);
		this.restTemplate = restTemplateBuilder.build();
	}

	@Async
	public CompletableFuture<StateDto> sendImageToDisplayAsync(Connection connection, boolean inverted) throws IOException {
		StateDto stateDto;
		Template template = connection.getDisplay().getTemplate();
		if (!proxyEnabled) {
			final String uri = "http://" + connection.getNetworkAddress();
			String image = imageUtil.getCodeFromImage(template.getImage());
			stateDto = restTemplate.postForObject(uri, image, StateDto.class);
		} else {
			String image = imageUtil.getBinaryImage(template.getImage(), false, connection.getDisplay().getResolution());
			final String uri = proxyIpAddress + "/send?ip=" + connection.getNetworkAddress();
			ImageDto imageDto = new ImageDto(image);
			stateDto = restTemplate.postForObject(uri, imageDto, StateDto.class);
		}
		return CompletableFuture.completedFuture(stateDto);
	}

	public StateDto sendImageToDisplay(Connection connection, boolean inverted) throws IOException {
		StateDto stateDto;
		Template template = connection.getDisplay().getTemplate();
		try {
			if (webSocketEnabled) {
				String image = imageUtil.getBinaryImage(template.getImage(),inverted,connection.getDisplay().getResolution());
//				webSocket.convertAndSend("/topic/send-image",image +":" +uri);
				webSocket.convertAndSend("/topic/send-image",new WSImageDto(connection.getNetworkAddress(),image, template.getUuid()));
				stateDto = new StateDto(); //TODO return state from websocket, but how?
			}
			else if (!proxyEnabled) {
				final String uri = "http://" + connection.getNetworkAddress();

				// get Code from image is C array like in first proitype
				String image = imageUtil.getBinaryImage(template.getImage(),false,connection.getDisplay().getResolution());

				stateDto = restTemplate.postForObject(uri, image, StateDto.class);
			}
			else {
				String image = imageUtil.getBinaryImage(template.getImage(),false,connection.getDisplay().getResolution());
				final String uri = proxyIpAddress + "/send?ip=" + connection.getNetworkAddress();
				ImageDto imageDto = new ImageDto(image);
				stateDto = restTemplate.postForObject(uri, imageDto, StateDto.class);
			}
		} catch (ResourceAccessException e) {
			stateDto = new StateDto("Display not reachable");
			e.printStackTrace();
		} catch (HttpServerErrorException e) {
			e.printStackTrace();
			stateDto = new StateDto("Sending failed, please try again");
		}
		return stateDto;
	}

	public StateDto clearDisplay(Connection connection) {
		StateDto stateDto;
		try {
			if (webSocketEnabled) {
				webSocket.convertAndSend("/topic/clear",new WSRequestDto(connection.getNetworkAddress()));
				stateDto = new StateDto(); //TODO return state from websocket, but how?
			}
			else if (!proxyEnabled) {
				final String uri = "http://" + connection.getNetworkAddress();
				stateDto = restTemplate.postForObject(uri, "2", StateDto.class);
			} else {
				final String uri = proxyIpAddress + "/clear?ip=" + connection.getNetworkAddress();
				stateDto = restTemplate.postForObject(uri, "2", StateDto.class);
			}
		} catch (ResourceAccessException e) {
			stateDto = new StateDto("Display not reachable");
			e.printStackTrace();
		}
		return stateDto;
	}

	public StateDto getCurrentState(Connection connection) {
		StateDto stateDto;
		try {
			if (webSocketEnabled){
				webSocket.convertAndSend("/topic/state",new WSRequestDto(connection.getNetworkAddress()));
				stateDto = new StateDto(); //TODO return state from websocket, but how?
			}
			else if (!proxyEnabled) {
				final String uri = "http://" + connection.getNetworkAddress();
				stateDto = restTemplate.postForObject(uri, "3", StateDto.class);//2 means clear display

			} else {
				final String uri = proxyIpAddress + "/state?ip=" + connection.getNetworkAddress();
				stateDto = restTemplate.postForObject(uri, "3", StateDto.class);//2 means clear display
			}
		} catch (ResourceAccessException e) {
			e.printStackTrace();
			stateDto = new StateDto("Display not reachable");
		}
		return stateDto;
	}

	public String testProxy() {
		try {
			if (!proxyEnabled) {
				return "PROXY not enabled. See API configuration for details...";
			}

			final String uri = proxyIpAddress + "/test";
			System.out.println(uri);
			return  restTemplate.getForObject(uri, String.class);
		} catch (ResourceAccessException e) {
			return "Proxy not reachable";
		}
	}

	public void setProxyIpAddress(String proxyIpAddress) {
		this.proxyIpAddress = proxyIpAddress;
	}
}
