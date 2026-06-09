// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class VenueDto {

    @JsonProperty("Id")
    private String Id;

    @JsonProperty("RoomDetails")
    private List<RoomDetailDto> RoomDetails;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public List<RoomDetailDto> getRoomDetails() {
        return RoomDetails;
    }

    public void setRoomDetails(List<RoomDetailDto> roomDetails) {
        this.RoomDetails = roomDetails;
    }
}