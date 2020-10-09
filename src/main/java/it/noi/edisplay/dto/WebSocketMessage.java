package it.noi.edisplay.dto;

public class WebSocketMessage {

    private String from;
    private String text;

    public WebSocketMessage(String from,String text){
    	this.text = text;
    	this.from = from;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
