package server.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.entity.User;
import server.entity.Room;
import server.entity.RoomMember;
import server.Repository.UserRepository;
import server.Repository.RoomRepository;
import server.Repository.RoomMemberRepository;
import server.security.JwtUtil;

import java.security.SecureRandom;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserRoomController {

    private final UserRepository userRepo;
    private final RoomRepository roomRepo;
    private final RoomMemberRepository roomMemberRepo;
    private final JwtUtil jwtUtil;

    // ✅ lấy từ Spring bean, không tự new
    private final BCryptPasswordEncoder passwordEncoder;

    public UserRoomController(UserRepository userRepo,
                              RoomRepository roomRepo,
                              RoomMemberRepository roomMemberRepo,
                              JwtUtil jwtUtil,
                              BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roomRepo = roomRepo;
        this.roomMemberRepo = roomMemberRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /* ---------- USER ---------- */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (user.getRole() == null || user.getRole().isEmpty()) user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userRepo.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> login) {
        String username = login.get("username");
        String password = login.get("password");
        Optional<User> u = userRepo.findByUsername(username);
        if (u.isEmpty() || !passwordEncoder.matches(password, u.get().getPassword())) {
            return ResponseEntity.status(401).body("Invalid credentials!");
        }
        String accessToken = jwtUtil.generateToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        Map<String, Object> resp = new HashMap<>();
        resp.put("accessToken", accessToken);
        resp.put("refreshToken", refreshToken);
        resp.put("username", username);
        resp.put("role", u.get().getRole());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            // Nếu refresh token hợp lệ thì cấp access token mới
            if (!jwtUtil.isTokenExpired(refreshToken)) {
                String newAccess = jwtUtil.generateToken(username);
                Map<String, Object> resp = new HashMap<>();
                resp.put("accessToken", newAccess);
                resp.put("refreshToken", refreshToken); // vẫn dùng refresh cũ
                return ResponseEntity.ok(resp);
            }
            return ResponseEntity.status(401).body("Refresh token expired");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }
    }


    /* ---------- ROOM ---------- */
    @PostMapping("/room/create")
    public ResponseEntity<?> createRoom(HttpServletRequest request) {
        String username = getUsernameFromAuth(request);
        User host = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = new Room();
        room.setHost(host);
        room.setCode(generateUniqueRoomCode());
        room.setAccessMode("OPEN");
        roomRepo.save(room);

        RoomMember member = new RoomMember();
        member.setRoom(room);
        member.setUser(host);
        member.setRole("OWNER");
        roomMemberRepo.save(member);

        return ResponseEntity.ok(room);
    }

    @PostMapping("/room/join/{code}")
    public ResponseEntity<?> joinRoom(@PathVariable String code, HttpServletRequest request) {
        String username = getUsernameFromAuth(request);

        Room room = roomRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        if (!room.isActive()) {
            return ResponseEntity.badRequest().body("Room inactive!");
        }

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean exists = roomMemberRepo.findAll().stream()
                .anyMatch(m -> m.getRoom().getId().equals(room.getId())
                        && m.getUser().getId().equals(user.getId()));
        if (!exists) {
            RoomMember member = new RoomMember();
            member.setRoom(room);
            member.setUser(user);
            member.setRole("PARTICIPANT");
            roomMemberRepo.save(member);
        }

        return ResponseEntity.ok("Join success! Room code: " + code);
    }

    @GetMapping("/room/list")
    public ResponseEntity<?> listRooms(HttpServletRequest request) {
        String username = getUsernameFromAuth(request);
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // lấy tất cả phòng mà user tham gia
        List<RoomMember> memberships = roomMemberRepo.findAll();
        List<Room> rooms = new ArrayList<>();
            for (RoomMember m : memberships) {
                if (m.getUser().getId().equals(user.getId())) {
                    rooms.add(m.getRoom());
                }
            }
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/room/leave/{code}")
    public ResponseEntity<?> leaveRoom(@PathVariable String code, HttpServletRequest request) {
        String username = getUsernameFromAuth(request);
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // xóa user khỏi room
        roomMemberRepo.findAll().stream()
                .filter(m -> m.getRoom().getId().equals(room.getId()) &&
                            m.getUser().getId().equals(user.getId()))
                .findFirst()
                .ifPresent(roomMemberRepo::delete);

        return ResponseEntity.ok("Left room " + code);
    }

    @PostMapping("/room/close/{code}")
    public ResponseEntity<?> closeRoom(@PathVariable String code, HttpServletRequest request) {
        String username = getUsernameFromAuth(request);
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepo.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // kiểm tra role = OWNER
        boolean isOwner = roomMemberRepo.findAll().stream()
                .anyMatch(m -> m.getRoom().getId().equals(room.getId()) &&
                            m.getUser().getId().equals(user.getId()) &&
                            "OWNER".equals(m.getRole()));
            if (!isOwner) {
            return ResponseEntity.status(403).body("Only OWNER can close room");
            }

        room.setActive(false);
        roomRepo.save(room);
        return ResponseEntity.ok("Room closed: " + code);
    }



    /* ---------- helpers ---------- */
    private String getUsernameFromAuth(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = header.substring(7);
        return jwtUtil.extractUsername(token);
    }

    private String generateUniqueRoomCode() {
        String code;
        do { code = generateRoomCode(); }
        while (roomRepo.findByCode(code).isPresent());
        return code;
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
