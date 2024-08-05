// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * DTO to read Events from OpenDataHub opendatahub.com
 */
public class EventDto implements Comparable<EventDto> {

	@JsonProperty("SpaceDesc")
	private String spaceDesc;

    @JsonProperty("SpaceDescList")
	private ArrayList<String> spaceDescList;

    @JsonProperty("EventDescriptionEN")
	private String eventDescriptionEN;

    @JsonProperty("EventDescriptionDE")
    private String eventDescriptionDE;

    @JsonProperty("EventDescriptionIT")
    private String eventDescriptionIT;

	@JsonProperty("Subtitle")
	private String subtitle;

	@JsonProperty("CompanyName")
	private String companyName;

	@JsonProperty("RoomStartDateUTC")
	private Long roomStartDateUTC;

	@JsonProperty("RoomEndDateUTC")
	private Long roomEndDateUTC;

    @JsonProperty("RoomStartDate")
    private String roomStartDate;

    @JsonProperty("RoomEndDate")
    private String roomEndDate;

    @JsonProperty("EventId")
    private Integer eventId;

	public String getSpaceDesc() {
		return spaceDesc;
	}

	public void setSpaceDesc(String spaceDesc) {
		this.spaceDesc = spaceDesc;
	}

    public ArrayList<String> getSpaceDescList() {
        return spaceDescList;
    }

    public void setSpaceDescList(ArrayList<String> spaceDescList) {
        this.spaceDescList = spaceDescList;
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

    public String getRoomStartDate() {
        return roomStartDate;
    }

    public void setRoomStartDate(String roomStartDate) {
        this.roomStartDate = roomStartDate;
    }

    public String getRoomEndDate() {
        return roomEndDate;
    }

    public void setRoomEndDate(String roomEndDate) {
        this.roomEndDate = roomEndDate;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

    @Override
    public int compareTo(EventDto o) {
      return getRoomStartDateUTC().compareTo(o.getRoomStartDateUTC());
    }

    public String getEventDescriptionDE() {
        return eventDescriptionDE;
    }

    public void setEventDescriptionDE(String eventDescriptionDE) {
        this.eventDescriptionDE = eventDescriptionDE;
    }

    public String getEventDescriptionIT() {
        return eventDescriptionIT;
    }

    public void setEventDescriptionIT(String eventDescriptionIT) {
        this.eventDescriptionIT = eventDescriptionIT;
    }

}
