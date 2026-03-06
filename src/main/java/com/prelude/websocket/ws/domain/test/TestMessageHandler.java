package com.prelude.websocket.ws.domain.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.prelude.websocket.ws.MainWebSocketHandler;
import com.prelude.websocket.ws.domain.DomainHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
@Component
public class TestMessageHandler implements DomainHandler {

    private final MainWebSocketHandler mainHandler; // inject để gọi broadcast

    public TestMessageHandler( @Lazy MainWebSocketHandler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public String getDomain() {
        return "test";
    }

    @Override
    public void handle(WebSocketSession session, JsonNode message) throws Exception {
        String action = message.get("action").asText();
        if ("printSession".equals(action)) {
            String text = message.has("text") ? message.get("text").asText() : "empty";
            System.out.println("Received: " + text);
            // broadcast cho tất cả client
            mainHandler.broadcast(text,session);
        } else {
            session.sendMessage(new TextMessage("Unknown action in test domain"));
        }
    }
}