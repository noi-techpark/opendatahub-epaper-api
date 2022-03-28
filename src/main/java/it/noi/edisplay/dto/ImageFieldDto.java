package it.noi.edisplay.dto;

import java.sql.Date;

import io.swagger.annotations.ApiModelProperty;
import it.noi.edisplay.model.ImageFieldType;

public class ImageFieldDto {
    @ApiModelProperty(hidden = true)
    private String uuid;
    @ApiModelProperty(hidden = true)
    private Date created;
    @ApiModelProperty(hidden = true)
    private Date lastUpdate;

    private Integer xPos;

    private Integer yPos;

    private String customText;
    
    private Integer fontSize;

    private ImageFieldType fieldType;
    
    private Integer height;

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

    public Integer getxPos() {
        return xPos;
    }

    public void setxPos(Integer xPos) {
        this.xPos = xPos;
    }

    public Integer getyPos() {
        return yPos;
    }

    public void setyPos(Integer yPos) {
        this.yPos = yPos;
    }

    public String getCustomText() {
        return customText;
    }

    public void setCustomText(String customText) {
        this.customText = customText;
    }

    public ImageFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(ImageFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
