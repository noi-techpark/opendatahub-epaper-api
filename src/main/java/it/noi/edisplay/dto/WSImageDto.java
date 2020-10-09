package it.noi.edisplay.dto;

public class WSImageDto {

    private String ip;
    private String image;

    public WSImageDto(final String ip, final String image) {
    	this.ip = ip;
    	this.image = image;
    }

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
}
