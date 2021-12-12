package it.noi.edisplay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NOIPlaceData {

    private String scode;

    @JsonProperty("smetadata.name.it")
    private String name;

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
}
