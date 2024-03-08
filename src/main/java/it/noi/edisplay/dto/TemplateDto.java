// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto;

import java.sql.Date;

import io.swagger.annotations.ApiModelProperty;

public class TemplateDto {

    private String name;

    private String description;

    private boolean footer;

    private boolean header;

    private boolean multipleRoom;

    private ResolutionDto resolution;

    private int[] roomData;
    private DisplayContentDto displayContent;

    @ApiModelProperty(hidden = true)
    private String uuid;
    @ApiModelProperty(hidden = true)
    private Date created;
    @ApiModelProperty(hidden = true)
    private Date lastUpdate;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DisplayContentDto getDisplayContent() {
        return displayContent;
    }

    public void setDisplayContent(DisplayContentDto displayContent) {
        this.displayContent = displayContent;
    }

    public boolean getFooter() {
        return footer;
    }

    public void setFooter(boolean footer) {
        this.footer = footer;
    }

    public boolean getHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public boolean getMultipleRoom() {
        return multipleRoom;
    }

    public void setMultipleRoom(boolean multipleRoom) {
        this.multipleRoom = multipleRoom;
    }

    public int[] getRoomData() {
        return roomData;
    }

    public void setRoomData(int[] roomData) {
        this.roomData = roomData;
    }

    public ResolutionDto getResolution() {
        return resolution;
    }

    public void setResolution(ResolutionDto resolution) {
        this.resolution = resolution;
    }
}
