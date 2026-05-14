// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto.support;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventDateDto {

    @JsonProperty("FromUTC")
    private Long fromUTC;

    @JsonProperty("ToUTC")
    private Long toUTC;

    @JsonProperty("From")
    private String from;

    @JsonProperty("To")
    private String to;

    @JsonProperty("VenueRoomDetailsIds")
    private List<String> venueRoomDetailsIds;

    @JsonProperty("EventDateAdditionalInfo")
    private AdditionalInfoDto eventDateAdditionalInfo;

    public Long getFromUTC() {
        return fromUTC;
    }

    public void setFromUTC(Long fromUTC) {
        this.fromUTC = fromUTC;
    }

    public Long getToUTC() {
        return toUTC;
    }

    public void setToUTC(Long toUTC) {
        this.toUTC = toUTC;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getVenueRoomDetailsIds() {
        return venueRoomDetailsIds;
    }

    public void setVenueRoomDetailsIds(List<String> venueRoomDetailsIds) {
        this.venueRoomDetailsIds = venueRoomDetailsIds;
    }

    public AdditionalInfoDto getEventDateAdditionalInfo() {
        return eventDateAdditionalInfo;
    }

    public void setEventDateAdditionalInfo(AdditionalInfoDto eventDateAdditionalInfo) {
        this.eventDateAdditionalInfo = eventDateAdditionalInfo;
    }
}