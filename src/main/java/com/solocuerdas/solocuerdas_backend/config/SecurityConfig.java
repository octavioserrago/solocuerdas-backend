package com.solocuerdas.solocuerdas_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SECURITY CONFIGURATION
 * Temporarily disabling security for development/testing
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for testing
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests without authentication
                );

        return http.build();
    }

    /**
     * PASSWORD ENCODER
     * BCrypt is a strong hashing algorithm for passwords
     * It automatically adds salt and is resistant to brute-force attacks
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
