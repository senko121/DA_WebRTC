package server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import server.entity.User;
import server.security.JwtUtil;
import server.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;  // ✅ inject bean JwtUtil

    // Đăng ký tài khoản
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    // Đăng nhập -> trả về JWT
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        return userService.findByUsername(username)
                .filter(u -> encoder.matches(password, u.getPassword()))
                .map(u -> Map.of(
                        "token", jwtUtil.generateToken(username), // ✅ dùng instance
                        "role", u.getRole()
                ))
                .orElseThrow(() -> new RuntimeException("Invalid credentials!"));
    }
}
