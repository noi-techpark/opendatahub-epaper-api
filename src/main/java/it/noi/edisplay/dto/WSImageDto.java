package it.noi.edisplay.dto;

public class WSImageDto {

    private String ip;
    private String image;
	private String name;

    public WSImageDto(final String ip, final String image, final String name) {
    	this.ip = ip;
    	this.image = image;
		this.name = name;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
