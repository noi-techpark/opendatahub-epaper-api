package it.noi.edisplay.messages;

public class WebSocketOutputMessage {

    private String from;
    private String text;
    private String time;

    public WebSocketOutputMessage(final String from, final String text, final String time) {

        this.from = from;
        this.text = text;
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getFrom() {
        return from;
    }
}
