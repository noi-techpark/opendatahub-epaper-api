// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NOIPlaceData {

    private String scode;

    @JsonProperty("smetadata.name.en")
    private String name;
    
    @JsonProperty("smetadata.room_label")
    private String roomLabel;

    @JsonProperty("smetadata.todaynoibzit")
    private String todaynoibzit;

    public String getScode() {
        return scode;
    }

    public void setScode(String scode) {
        this.scode = scode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomLabel() {
        return roomLabel;
    }

    public void setRoomLabel(String roomLabel) {
        this.roomLabel = roomLabel;
    }

    public String getTodaynoibzit() {
        return todaynoibzit;
    }

    public void setTodaynoibzit(String todaynoibzit) {
        this.todaynoibzit = todaynoibzit;
    }
}
