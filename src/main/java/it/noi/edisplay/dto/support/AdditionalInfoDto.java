// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto.support;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdditionalInfoDto {
    
    @JsonProperty("en")
    private AdditionalLangDto en;

    public AdditionalLangDto getEn() {
        return en;
    }

    public void setEn(AdditionalLangDto en) {
        this.en = en;
    }

    @JsonProperty("de")
    private AdditionalLangDto de;

    public AdditionalLangDto getDe() {
        return de;
    }

    public void setDe(AdditionalLangDto de) {
        this.de = de;
    }

    @JsonProperty("it")
    private AdditionalLangDto it;
    
    public AdditionalLangDto getIt() {
        return it;
    }

    public void setIt(AdditionalLangDto it) {
        this.it = it;
    }
}