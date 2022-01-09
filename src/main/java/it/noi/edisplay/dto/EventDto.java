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
}
