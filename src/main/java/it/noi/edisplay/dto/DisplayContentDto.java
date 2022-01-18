package it.noi.edisplay.dto;

import java.sql.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class DisplayContentDto {
    @ApiModelProperty(hidden = true)
    private String uuid;
    @ApiModelProperty(hidden = true)
    private Date created;
    @ApiModelProperty(hidden = true)
    private Date lastUpdate;

    private List<ImageFieldDto> imageFields;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public List<ImageFieldDto> getImageFields() {
        return imageFields;
    }

    public void setImageFields(List<ImageFieldDto> imageFields) {
        this.imageFields = imageFields;
    }
}
