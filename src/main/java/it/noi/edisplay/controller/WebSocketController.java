package it.noi.edisplay.controller;

// import com.google.gson.Gson;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController  {

	// private Gson gson = new Gson();

	@MessageMapping("/state")
//	@SendTo("/topic/state")
	public void getState(String stateDto) {
		System.out.println(stateDto);
//		final String time = new SimpleDateFormat("HH:mm").format(new Date());
//		return new WebSocketOutputMessage(message, time);
	}

//	@MessageMapping("/state")
//	@SendTo("/topic/send-image")
//	public WebSocketOutputMessage sendImage(final WebSocketMessage message) throws Exception {
//
//		final String time = new SimpleDateFormat("HH:mm").format(new Date());
//		return new WebSocketOutputMessage(message.getFrom(), message.getText(), time);
//	}

}
