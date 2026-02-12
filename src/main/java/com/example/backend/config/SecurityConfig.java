package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // PUBLIC AUTH (if you ever add login)
                .requestMatchers("/api/auth/**").permitAll()

                // Appointments and admin endpoints controlled by secret key in controller
                .requestMatchers("/api/appointments/**").permitAll()
                .requestMatchers("/api/admin/**").permitAll()

                .anyRequest().permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .invalidateHttpSession(true)
            );

        return http.build();
    }
}
