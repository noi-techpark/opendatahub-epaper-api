package it.noi.edisplay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	@JsonProperty("RoomStartDateUTC")
	private Long roomStartDateUTC;

	@JsonProperty("RoomEndDateUTC")
	private Long roomEndDateUTC;

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

	public Long getRoomStartDateUTC() {
		return roomStartDateUTC;
	}

	public void setRoomStartDateUTC(Long roomStartDateUTC) {
		this.roomStartDateUTC = roomStartDateUTC;
	}

	public Long getRoomEndDateUTC() {
		return roomEndDateUTC;
	}

	public void setRoomEndDateUTC(Long roomEndDateUTC) {
		this.roomEndDateUTC = roomEndDateUTC;
	}
}
