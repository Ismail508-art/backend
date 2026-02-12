package com.example.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.SignupRequest;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===== SIGNUP =====
    public String signup(SignupRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already exists";
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            return "Phone already exists";
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // ✅ ENCODE PASSWORD
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Optional: default user is NOT admin
        user.setAdmin(false);

        userRepository.save(user);
        return "Signup successful";
    }

    // ===== LOGIN =====
    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmailOrPhone(
                        request.getEmailOrPhone(),
                        request.getEmailOrPhone()
                )
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials")
                );

        // ✅ MATCH ENCODED PASSWORD
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new RuntimeException("Invalid credentials");
        }

        // ✅ RETURN isAdmin IN DTO
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.isAdmin(), // 🔥 NEW
                null            // token later
        );
    }
}
