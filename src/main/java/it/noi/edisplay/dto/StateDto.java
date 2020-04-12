package it.noi.edisplay.dto;

import java.util.Date;

public class StateDto {


	boolean sleeping;
	boolean hasImage;
	int width;
	int height;
	int batteryState;
	String ip;
	String mac;
	Date lastState;
	String errorMessage;

	public StateDto(){

	}

	public StateDto(String errorMessage){
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public boolean isSleeping() {
		return sleeping;
	}

	public void setSleeping(boolean sleeping) {
		this.sleeping = sleeping;
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


	public Date getLastState() {
		return lastState;
	}

	public void setLastState(Date lastState) {
		this.lastState = lastState;
	}
}
