package it.noi.edisplay.controller;

import com.google.gson.Gson;
import it.noi.edisplay.messages.WebSocketMessage;
import it.noi.edisplay.messages.WebSocketOutputMessage;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
public class WebSocketController  {

	private Gson gson = new Gson();

	@MessageMapping("/state")
	@SendTo("/topic/state")
	public WebSocketOutputMessage getState(final String message) throws Exception {

		final String time = new SimpleDateFormat("HH:mm").format(new Date());
		return new WebSocketOutputMessage(message, message, time);
	}

//	@MessageMapping("/state")
//	@SendTo("/topic/send-image")
//	public WebSocketOutputMessage sendImage(final WebSocketMessage message) throws Exception {
//
//		final String time = new SimpleDateFormat("HH:mm").format(new Date());
//		return new WebSocketOutputMessage(message.getFrom(), message.getText(), time);
//	}

}
