package com.example.backend.service;

import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    @Autowired(required = false)
    private JavaMailSender mailSender; // may be null in dev

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Transactional
    public boolean generateResetTokenAndSendEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        // Check for existing token
        Optional<PasswordResetToken> existingTokenOpt = tokenRepository.findByUser(user);

        PasswordResetToken resetToken = existingTokenOpt
                .map(token -> {
                    token.setToken(UUID.randomUUID().toString());
                    token.setExpiryDate(LocalDateTime.now().plusHours(1));
                    return tokenRepository.save(token);
                })
                .orElseGet(() -> {
                    PasswordResetToken token = new PasswordResetToken();
                    token.setUser(user);
                    token.setToken(UUID.randomUUID().toString());
                    token.setExpiryDate(LocalDateTime.now().plusHours(1));
                    return tokenRepository.save(token);
                });

        String resetUrl = "http://localhost:5173/reset-password?token=" + resetToken.getToken();

        if ("prod".equalsIgnoreCase(activeProfile) && mailSender != null) {
            // ✅ Production: send real email
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Password Reset Request");
                message.setText("Hello " + user.getName() + ",\n\nClick this link to reset your password:\n\n" + resetUrl);
                mailSender.send(message);
            } catch (Exception e) {
                e.printStackTrace();
                return false; // Email sending failed
            }
        } else {
            // 🔹 Dev: just print link
            System.out.println("Password reset link (dev): " + resetUrl);
        }

        return true;
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) return false;

        User user = resetToken.getUser();
        user.setPassword(newPassword); // Ideally, hash password in production
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return true;
    }
}
