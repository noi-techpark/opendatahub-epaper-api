package it.noi.edisplay.messages;

public class WebSocketMessage {

    private String from;
    private String text;

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
