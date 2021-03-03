package it.noi.edisplay.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class DisplayDto {

	private String name;
	private byte[] image;
	private int batteryPercentage;
	private ResolutionDto resolution;
	private TemplateDto template;
	private String mac;

	@ApiModelProperty(hidden = true)
	private ConnectionDto connection;

	@ApiModelProperty(hidden = true)
	private String uuid;

	@ApiModelProperty(hidden = true)
	private Date created;

	@ApiModelProperty(hidden = true)
	private Date lastUpdate;

	@ApiModelProperty(hidden = true)
	private Date lastState;

	@ApiModelProperty(hidden = true)
	private Date lastRealDisplayState;

	public Date getLastRealDisplayState() {
		return lastRealDisplayState;
	}

	public TemplateDto getTemplate() {
		return template;
	}

	public void setTemplate(TemplateDto template) {
		this.template = template;
	}

	public void setLastRealDisplayState(Date lastRealDisplayState) {
		this.lastRealDisplayState = lastRealDisplayState;
	}

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

	public Date getLastState() {
		return lastState;
	}

	public void setLastState(Date lastState) {
		this.lastState = lastState;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public int getBatteryPercentage() {
		return batteryPercentage;
	}

	public void setBatteryPercentage(int batteryPercentage) {
		this.batteryPercentage = batteryPercentage;
	}

	public ConnectionDto getConnection() {
		return connection;
	}

	public void setConnection(ConnectionDto connection) {
		this.connection = connection;
	}

	public ResolutionDto getResolution() {
		return resolution;
	}


	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public void setResolution(ResolutionDto resolution) {
		this.resolution = resolution;
	}
}
