package it.noi.edisplay.dto;


import io.swagger.annotations.ApiModelProperty;

import java.sql.Date;


public class LocationDto {

	private String name;

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

}
