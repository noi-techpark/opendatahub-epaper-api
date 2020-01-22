package it.noi.edisplay.dto;


import java.sql.Date;



public class ConnectionDto {

    private String name;
    private String uuid;
    private String networkAddress;
    private String coordinates;
    private Date created;
    private Date lastUpdate;
    private String displayUuid;
    private String locationUuid;

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

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }
}
