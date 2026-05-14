// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto.support;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoomDetailDto {

    @JsonProperty("Id")
    private String Id;

    @JsonProperty("Shortname")
    private String Shortname;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getShortname() {
        return Shortname;
    }

    public void setShortname(String shortname) {
        this.Shortname = shortname;
    }
}