// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.services;


import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.dto.support.ResponseDto;
import it.noi.edisplay.dto.NOIPlaceData;
import it.noi.edisplay.dto.NOIPlaceDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class OpenDataRestService {

	private final RestTemplate restTemplate;

	@Value("${open.data.events.url}")
	private String eventsUrl;

	@Value("${open.data.places.url}")
	private String placesUrl;

	@Value("${event.offset}")
	private int eventOffset;

	public OpenDataRestService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	public List<EventDto> getEvents() {
		ArrayList<EventDto> result = new ArrayList<>();
		String urlWithTimestamp = String.format(eventsUrl, new Date().getTime() + eventOffset * 60000);
		ResponseDto response = restTemplate.getForObject(urlWithTimestamp, ResponseDto.class);
		if (response != null) {
			result = new ArrayList<>(response.getItems());
		}
		return result;
	}

	public List<NOIPlaceData> getNOIPlaces() {
	    NOIPlaceDto places = restTemplate.getForObject(placesUrl, NOIPlaceDto.class);
	    if (places != null) {
	        return places.getData();
	    }
		return new ArrayList<>();
	}
}
