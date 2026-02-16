package com.example.backend.service;

import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public boolean generateResetTokenAndSendEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        // Check if token already exists
        Optional<PasswordResetToken> existingTokenOpt = tokenRepository.findByUser(user);

        PasswordResetToken resetToken = existingTokenOpt
                .map(token -> {
                    // Update existing token
                    token.setToken(UUID.randomUUID().toString());
                    token.setExpiryDate(LocalDateTime.now().plusHours(1));
                    return tokenRepository.save(token);
                })
                .orElseGet(() -> {
                    // Create new token
                    PasswordResetToken token = new PasswordResetToken();
                    token.setUser(user);
                    token.setToken(UUID.randomUUID().toString());
                    token.setExpiryDate(LocalDateTime.now().plusHours(1));
                    return tokenRepository.save(token);
                });

        // 🔹 TODO: Send token link via email (e.g., example.com/reset-password?token=xxx)
        System.out.println("Password reset link: http://localhost:5173/reset-password?token=" + resetToken.getToken());

        return true;
    }

    // 🔹 Reset password using token
    @Transactional
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
