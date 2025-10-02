package server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.Repository.RoomRepository;
import server.entity.Room;
import server.entity.User;

import java.security.SecureRandom;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepo;

    public Room createRoom(User host) {
        Room room = new Room();
        room.setHost(host);
        room.setCode(generateRoomCode());
        room.setActive(true);
        return roomRepo.save(room);
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
