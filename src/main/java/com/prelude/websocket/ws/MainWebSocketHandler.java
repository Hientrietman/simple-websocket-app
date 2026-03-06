package com.prelude.websocket.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MainWebSocketHandler extends TextWebSocketHandler {

    private final MessageDispatcher dispatcher;
    private final ObjectMapper mapper = new ObjectMapper();

    // Dùng để quản lý tất cả session đang mở
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public MainWebSocketHandler(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    // Khi client connect thành công
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("✅ Connected: " + session.getId() + " | " + session.getRemoteAddress());
        session.sendMessage(new TextMessage(
                mapper.writeValueAsString(Map.of(
                        "type", "sessionInfo",
                        "sessionId", session.getId()
                ))
        ));
    }

    // Xử lý message từ client
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("📩 Received from " + session.getId() + ": " + message.getPayload());

        try {
            JsonNode json = mapper.readTree(message.getPayload());
            dispatcher.dispatch(session, json); // gửi cho dispatcher xử lý
        } catch (IOException e) {
            System.err.println("⚠ Failed to parse message: " + e.getMessage());
            session.sendMessage(new TextMessage("Invalid message format"));
        }
    }

    // Khi xảy ra lỗi trên connection
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("⚠ Transport error on session " + session.getId() + ": " + exception.getMessage());
        super.handleTransportError(session, exception);
    }

    // Khi client disconnect
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("❌ Disconnected: " + session.getId() + " | " + status);
        super.afterConnectionClosed(session, status);
    }

    // helper broadcast
    public void broadcast(String payload, WebSocketSession session) {
        sessions.forEach(s -> {
            if (s.isOpen() && !session.getId().equals(s.getId())) {
                try {
                    s.sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    System.err.println("⚠ Failed to send to session " + s.getId());
                }
            }
        });
    }
}