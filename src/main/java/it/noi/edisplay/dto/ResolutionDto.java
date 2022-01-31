package it.noi.edisplay.dto;

import io.swagger.annotations.ApiModelProperty;

public class ResolutionDto {
    @ApiModelProperty(hidden = true)
    private String uuid;

	private int width;

	private int height;
	
	private int bitDepth;

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

    public int getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(int bitDepth) {
        this.bitDepth = bitDepth;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
