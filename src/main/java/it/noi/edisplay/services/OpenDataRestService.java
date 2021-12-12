package it.noi.edisplay.services;


import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.dto.NOIPlaceDto;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

@Component
public class OpenDataRestService {

	private final RestTemplate restTemplate;
	private String eventsUrl = "https://tourism.opendatahub.bz.it/api/EventShort/GetbyRoomBooked?startdate=%s&eventlocation=NOI&datetimeformat=uxtimestamp&onlyactive=true";
	private String eventLocationUrl = "http://tourism.opendatahub.bz.it/api/EventShort/RoomMapping";
	private String placesUrl = "https://mobility.api.opendatahub.bz.it/v2/flat/NOI-Place?limit=0&select=scode,smetadata.name.it";


	public OpenDataRestService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public ArrayList<EventDto> getEvents() {
		ArrayList<EventDto> result = new ArrayList<>();
		String urlWithTimestamp = String.format(eventsUrl, new Date().getTime());
		EventDto[] eventDtos = restTemplate.getForObject(urlWithTimestamp, EventDto[].class);
		Collections.addAll(result, eventDtos);
		return result;
	}

	public ArrayList<String> getEventLocations() {
		ArrayList<String> result = new ArrayList<>();
		String eventLocationsRawString = restTemplate.getForObject(eventLocationUrl, String.class);

		eventLocationsRawString = eventLocationsRawString.substring(1, eventLocationsRawString.length() - 1 );
		for(String s : eventLocationsRawString.split(","))
			result.add(s.split(":")[0].replaceAll("\"", ""));

		return result;
	}
	
	public NOIPlaceDto getNOIPlaces() {
		return restTemplate.getForObject(placesUrl, NOIPlaceDto.class);
	}
}
