package com.example.api_gateway.controller;

import com.example.api_gateway.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // Dummy authentication
        if ("admin".equals(username) && "password".equals(password)) {
            String token = JwtUtil.generateToken(username);
            return Map.of("token", token);
        }

        return Map.of("error", "Invalid credentials");
    }
}
