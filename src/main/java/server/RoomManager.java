package server;

import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    // roomId -> danh sách client session
    private static final Map<String, List<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    // Thêm client vào room
    public static synchronized void addClientToRoom(String roomId, WebSocketSession session) {
        rooms.putIfAbsent(roomId, new ArrayList<>());
        rooms.get(roomId).add(session);
        System.out.println("Client " + session.getId() + " joined room " + roomId);
    }

    // Xóa client khi disconnect
    public static synchronized void removeClient(WebSocketSession session) {
        rooms.values().forEach(list -> list.remove(session));
        System.out.println("Client " + session.getId() + " removed from all rooms");
    }

    // Lấy danh sách client trong room (trừ chính session đang gửi)
    public static List<WebSocketSession> getOtherClients(String roomId, WebSocketSession self) {
        if (!rooms.containsKey(roomId)) return Collections.emptyList();

        List<WebSocketSession> others = new ArrayList<>();
        for (WebSocketSession s : rooms.get(roomId)) {
            if (s.isOpen() && !s.getId().equals(self.getId())) {
                others.add(s);
            }
        }
        return others;
    }

    // Debug: in ra tất cả rooms
    public static void printRooms() {
        rooms.forEach((room, sessions) -> {
            System.out.println("Room " + room + " có " + sessions.size() + " client");
        });
    }
}
