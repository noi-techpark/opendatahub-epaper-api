package it.noi.edisplay.services;

import it.noi.edisplay.model.Connection;
import it.noi.edisplay.model.Display;
import it.noi.edisplay.utils.ImageUtil;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestService {

    private final RestTemplate restTemplate;

    public RestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void sendImageToDisplay(Display display, Connection connection) {
        final String uri = connection.getNetworkAddress();
        String image = ImageUtil.getBinaryImage(display.getImage());
        restTemplate.postForLocation(uri, image);
    }

    public void clearDisplay(Connection connection){
        final String uri = connection.getNetworkAddress();
        restTemplate.postForLocation(uri, "2"); //2 means clear displa
    }
}