package com.prelude.websocket.ws.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

public interface DomainHandler {
    String getDomain();
    void handle(WebSocketSession session, JsonNode message) throws Exception;
}