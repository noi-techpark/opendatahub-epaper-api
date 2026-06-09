// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto.support;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizerInfosDto {
    @JsonProperty("en")
    private OrganizerLangDto en;

    public OrganizerLangDto getEn() {
        return en;
    }

    public void setEn(OrganizerLangDto en) {
        this.en = en;
    }
}