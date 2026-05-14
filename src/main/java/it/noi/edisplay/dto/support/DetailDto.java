// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto.support;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DetailDto {
    @JsonProperty("en")
    private LangDto en;

    @JsonProperty("de")
    private LangDto de;

    @JsonProperty("it")
    private LangDto it;

    public LangDto getEn() {
        return en;
    }

    public void setEn(LangDto en) {
        this.en = en;
    }

    public LangDto getDe() {
        return de;
    }

    public void setDe(LangDto de) {
        this.de = de;
    }

    public LangDto getIt() {
        return it;
    }

    public void setIt(LangDto it) {
        this.it = it;
    }
}