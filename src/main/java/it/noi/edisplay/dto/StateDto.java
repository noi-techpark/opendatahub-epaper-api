package it.noi.edisplay.dto;

import java.util.Date;

public class StateDto {


	boolean isSleeping;
	boolean hasImage;
	int batteryState;
	String ipAddress;
	String macAddress;
	Date lastState;

	public StateDto(String [] states){
		isSleeping = states[0].equals("1");
		hasImage = states[1].equals("1");
		batteryState = Integer.parseInt(states[2]);
		ipAddress = states[3];
		macAddress = states[4];
	}

	public boolean isSleeping() {
		return isSleeping;
	}

	public void setSleeping(boolean sleeping) {
		isSleeping = sleeping;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public int getBatteryState() {
		return batteryState;
	}

	public void setBatteryState(int batteryState) {
		this.batteryState = batteryState;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public Date getLastState() {
		return lastState;
	}

	public void setLastState(Date lastState) {
		this.lastState = lastState;
	}
}
