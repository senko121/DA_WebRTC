package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import server.security.JwtUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SignalingHandler extends TextWebSocketHandler {

    private static final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private static final Map<String, String> sessionUsers = new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private final JwtUtil jwtUtil;

    public SignalingHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = extractQueryParam(session, "token");

        try {
            if (token == null) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing token"));
                return;
            }
            String username = jwtUtil.extractUsername(token); // sẽ ném nếu hỏng
            sessionUsers.put(session.getId(), username);
            System.out.println("WS connected " + session.getId() + " user=" + username);
        } catch (Exception ex) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid token"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JsonNode json = mapper.readTree(payload);
        String type = json.path("type").asText("");
        String roomId = json.has("room") ? json.get("room").asText() : null;
        String user = sessionUsers.getOrDefault(session.getId(), "Unknown");

        if ("JOIN".equalsIgnoreCase(type) && roomId != null) {
            rooms.putIfAbsent(roomId, new ArrayList<>());
            rooms.get(roomId).add(session);

            Map<String, Object> resp = Map.of(
                    "type", "JOINED",
                    "room", roomId,
                    "user", user
            );
            String msg = mapper.writeValueAsString(resp);
            session.sendMessage(new TextMessage(msg));
            broadcast(roomId, session, msg);
            System.out.println("JOIN room=" + roomId + " by " + user);
            return;
        }

        if ("CHAT".equalsIgnoreCase(type) && roomId != null) {
            Map<String,Object> chat = Map.of(
                    "type","CHAT","room",roomId,"user",user,"text",json.path("text").asText("")
            );
            broadcast(roomId, session, mapper.writeValueAsString(chat));
            return;
        }

        // default: forward OFFER/ANSWER/CANDIDATE...
        if (roomId != null) {
            broadcast(roomId, session, payload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        rooms.values().forEach(list -> list.remove(session));
        String user = sessionUsers.remove(session.getId());
        System.out.println("WS closed " + session.getId() + " user=" + user + " reason=" + status);
    }

    private void broadcast(String roomId, WebSocketSession sender, String msg) throws Exception {
        List<WebSocketSession> list = rooms.get(roomId);
        if (list == null) return;
        for (WebSocketSession s : list) {
            if (s.isOpen() && !s.getId().equals(sender.getId())) {
                s.sendMessage(new TextMessage(msg));
            }
        }
    }

    private String extractQueryParam(WebSocketSession session, String name) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null) return null;
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
        }
}
