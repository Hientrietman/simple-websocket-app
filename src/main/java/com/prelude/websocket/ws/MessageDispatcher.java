package com.prelude.websocket.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.prelude.websocket.ws.domain.DomainHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MessageDispatcher {

    private final Map<String, DomainHandler> domainHandlers;

    public MessageDispatcher(List<DomainHandler> handlers) {
        this.domainHandlers = handlers.stream()
                .collect(Collectors.toMap(DomainHandler::getDomain, h -> h));
    }

    public void dispatch(WebSocketSession session, JsonNode json) throws Exception {
        String domain = json.get("domain").asText();
        DomainHandler handler = domainHandlers.get(domain);

        if (handler == null) {
            session.sendMessage(new TextMessage("Unknown domain"));
            return;
        }

        handler.handle(session, json);
    }
}