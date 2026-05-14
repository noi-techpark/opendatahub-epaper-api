// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto.support;

import it.noi.edisplay.dto.EventDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ResponseDto {

    @JsonProperty("Items")
    private List<EventDto> items;

    public List<EventDto> getItems() {
        return items;
    }

    public void setItems(List<EventDto> items) {
        this.items = items;
    }
}
