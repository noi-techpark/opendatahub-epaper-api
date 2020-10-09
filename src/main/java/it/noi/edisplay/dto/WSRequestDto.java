package it.noi.edisplay.dto;

public class WSRequestDto {


	private String ip;

	public WSRequestDto(final String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
