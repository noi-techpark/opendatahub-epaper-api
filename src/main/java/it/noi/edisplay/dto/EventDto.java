package it.noi.edisplay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * DTO to read Events from OpenDataHub opendatahub.bz.it
 */
public class EventDto {

	@JsonProperty("SpaceDesc")
	private String spaceDesc;

	@JsonProperty("EventDescriptionEN")
	private String eventDescriptionEN;

	@JsonProperty("CompanyName")
	private String companyName;

	@JsonProperty("EventStartDateUTC")
	private Long eventStartDateUTC;

	@JsonProperty("EventEndDateUTC")
	private Long eventEndDateUTC;

	public String getSpaceDesc() {
		return spaceDesc;
	}

	public void setSpaceDesc(String spaceDesc) {
		this.spaceDesc = spaceDesc;
	}

	public String getEventDescriptionEN() {
		return eventDescriptionEN;
	}

	public void setEventDescriptionEN(String eventDescriptionEN) {
		this.eventDescriptionEN = eventDescriptionEN;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public Long getEventStartDateUTC() {
		return eventStartDateUTC;
	}

	public void setEventStartDateUTC(Long eventStartDateUTC) {
		this.eventStartDateUTC = eventStartDateUTC;
	}

	public Long getEventEndDateUTC() {
		return eventEndDateUTC;
	}

	public void setEventEndDateUTC(Long eventEndDateUTC) {
		this.eventEndDateUTC = eventEndDateUTC;
	}
}
