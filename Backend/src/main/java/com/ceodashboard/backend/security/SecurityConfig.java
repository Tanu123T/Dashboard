package com.ceodashboard.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    writeJsonError(
                        response,
                        HttpStatus.UNAUTHORIZED,
                        "Authentication required",
                        request.getRequestURI()
                    )
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    writeJsonError(
                        response,
                        HttpStatus.FORBIDDEN,
                        "Access denied",
                        request.getRequestURI()
                    )
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Public routes
                .requestMatchers("/", "/health").permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/projects/**").permitAll()
                .requestMatchers("/sync/**").permitAll()
                .requestMatchers("/error", "/error/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                .anyRequest().permitAll()
            )

            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeJsonError(
        HttpServletResponse response,
        HttpStatus status,
        String message,
        String path
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(
            "{"
                + "\"timestamp\":\"" + escapeJson(Instant.now().toString()) + "\"," 
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + escapeJson(status.getReasonPhrase()) + "\"," 
                + "\"message\":\"" + escapeJson(message) + "\"," 
                + "\"path\":\"" + escapeJson(path) + "\""
                + "}"
        );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}