package com.example.backend.controller;

import com.example.backend.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    // 🔹 Request password reset
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean sent = passwordResetService.generateResetTokenAndSendEmail(email);
        if (sent) return ResponseEntity.ok("Reset link sent to your email");
        return ResponseEntity.badRequest().body("Email not found");
    }

    // 🔹 Reset password using token
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        boolean updated = passwordResetService.resetPassword(token, newPassword);
        if (updated) return ResponseEntity.ok("Password updated successfully");
        return ResponseEntity.badRequest().body("Invalid or expired token");
    }
}
