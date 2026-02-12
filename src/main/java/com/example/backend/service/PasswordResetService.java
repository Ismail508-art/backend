package com.example.backend.service;

import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    // 🔹 Generate token and "send email"
    public boolean generateResetTokenAndSendEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        // Generate random token
        String token = UUID.randomUUID().toString();

        // Set expiry 1 hour from now
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        tokenRepository.save(resetToken);

        // 🔹 TODO: Send token link via email (e.g., example.com/reset-password?token=xxx)
        System.out.println("Password reset link: http://localhost:5173/reset-password?token=" + token);

        return true;
    }

    // 🔹 Reset password using token
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) return false;

        User user = resetToken.getUser();
        user.setPassword(newPassword); // 🔹 Ideally hash password
        userRepository.save(user);

        // Delete token after use
        tokenRepository.delete(resetToken);

        return true;
    }
}
