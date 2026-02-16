package com.example.backend.service;

import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    // Brevo API Key from environment variable
    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    @Transactional
    public boolean generateResetTokenAndSendEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();

        // Check if token already exists
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

        // Build reset URL
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken.getToken();

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            String json = """
                {
                  "sender": { "name": "MyClinic", "email": "no-reply@myclinic.com" },
                  "to": [{"email": "%s"}],
                  "subject": "Password Reset Request",
                  "htmlContent": "<p>Click this link to reset your password: <a href='%s'>Reset Password</a></p>"
                }
            """.formatted(email, resetUrl);

            HttpEntity<String> request = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email", request, String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) return false;

        User user = resetToken.getUser();
        user.setPassword(newPassword); // You can hash this if needed
        userRepository.save(user);

        tokenRepository.delete(resetToken);
        return true;
    }
}
