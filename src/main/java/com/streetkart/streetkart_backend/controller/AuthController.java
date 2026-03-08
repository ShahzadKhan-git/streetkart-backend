package com.streetkart.streetkart_backend.controller;

import com.streetkart.streetkart_backend.dto.LoginRequest;
import com.streetkart.streetkart_backend.dto.RegisterRequest;
import com.streetkart.streetkart_backend.entity.Role;
import com.streetkart.streetkart_backend.entity.User;
import com.streetkart.streetkart_backend.repository.UserRepository;
import com.streetkart.streetkart_backend.security.JwtUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .pincode(request.getPincode())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        var userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }

        String token = JwtUtil.generateToken(user.getEmail());

        return ResponseEntity.ok(token);
    }
}