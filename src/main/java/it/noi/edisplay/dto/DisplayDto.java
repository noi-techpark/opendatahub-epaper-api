package it.noi.edisplay.dto;


import java.util.Date;
import java.util.UUID;

public class DisplayDto {

    private String uuid;
    private String name;
    private Date created;
    private Date lastUpdate;
    private Date lastState;
    private byte[] image;
    private int batteryPercentage;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setLastState(Date lastState) {
        this.lastState = lastState;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public Date getLastState() {
        return lastState;
    }

    public byte[] getImage() {
        return image;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

}
