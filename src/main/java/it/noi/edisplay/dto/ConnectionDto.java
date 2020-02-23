package it.noi.edisplay.dto;



import io.swagger.annotations.ApiModelProperty;

import java.sql.Date;



public class ConnectionDto {

    private String name;
	private String networkAddress;
	private Double longitude;
	private Double latitude;
	private String displayUuid;
	private String locationUuid;

	@ApiModelProperty(hidden=true)
    private String uuid;
	@ApiModelProperty(hidden=true)
    private Date created;
	@ApiModelProperty(hidden=true)
    private Date lastUpdate;

    public String getNetworkAddress() {
        return networkAddress;
    }



    public void setNetworkAddress(String networkAddress) {
        this.networkAddress = networkAddress;
    }

    public String getDisplayUuid() {
        return displayUuid;
    }

    public void setDisplayUuid(String displayUuid) {
        this.displayUuid = displayUuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
