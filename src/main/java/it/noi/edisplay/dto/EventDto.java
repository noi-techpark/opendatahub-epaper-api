// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.noi.edisplay.dto.support.AdditionalInfoDto;
import it.noi.edisplay.dto.support.AdditionalLangDto;
import it.noi.edisplay.dto.support.DetailDto;
import it.noi.edisplay.dto.support.EventDateDto;
import it.noi.edisplay.dto.support.LangDto;
import it.noi.edisplay.dto.support.OrganizerInfosDto;
import it.noi.edisplay.dto.support.OrganizerLangDto;

/**
 * DTO to read Events from OpenDataHub opendatahub.com
 */
public class EventDto implements Comparable<EventDto> {

	private String spaceDesc;
    
    @JsonProperty("EventId")
    private Integer eventId;

    @JsonProperty("Detail")
    private DetailDto detail;

    @JsonProperty("OrganizerInfos")
    private OrganizerInfosDto organizerInfos;

    @JsonProperty("EventDate")
    private List<EventDateDto> eventDate;

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public DetailDto getDetail() {
        return detail;
    }

    public void setDetail(DetailDto detail) {
        this.detail = detail;
    }

    public OrganizerInfosDto getOrganizerInfos() {
        return organizerInfos;
    }

    public void setOrganizerInfos(OrganizerInfosDto organizerInfos) {
        this.organizerInfos = organizerInfos;
    }

    public List<EventDateDto> getEventDate() {
        return eventDate;
    }

    public void setEventDate(List<EventDateDto> eventDate) {
        this.eventDate = eventDate;
    }

    
    @Override
    public int compareTo(EventDto o) {
        return this.getEventDate().get(0).getFromUTC()
            .compareTo(o.getEventDate().get(0).getFromUTC());
    }

	public String getSpaceDesc() {
		return spaceDesc;
	}

	public void setSpaceDesc(String spaceDesc) {
		this.spaceDesc = spaceDesc;
	}

}
