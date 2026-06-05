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

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

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
                .requestMatchers("/error", "/error/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Allow anonymous GET access to projects and sprints (list and details)
                .requestMatchers(HttpMethod.GET, "/projects/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/sprints/**").permitAll()

                // Allow public GET access to HRMS employee and analytics data (dashboard display)
                .requestMatchers(HttpMethod.GET, "/api/v1/hrms/employees", "/api/v1/hrms/employees/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/hrms/analytics", "/api/v1/hrms/analytics/**").permitAll()

                // Allow public GET access for internal DB inspection during development
                .requestMatchers(HttpMethod.GET, "/internal/db/**").permitAll()

                // Allow public GET access to sync endpoints for manual triggering during development
                .requestMatchers(HttpMethod.GET, "/sync/**").permitAll()

                // Require ADMIN role for mutating project endpoints
                .requestMatchers(HttpMethod.POST, "/projects/**").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/projects/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/projects/**").hasRole("admin")

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // realm_access.roles -> ROLE_{role}
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }

            // resource_access.<client>.roles -> ROLE_{role}
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                for (Object clientEntry : resourceAccess.values()) {
                    if (clientEntry instanceof Map) {
                        Map<?, ?> clientMap = (Map<?, ?>) clientEntry;
                        Object rolesObj = clientMap.get("roles");
                        if (rolesObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> clientRoles = (List<String>) rolesObj;
                            for (String role : clientRoles) {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            }
                        }
                    }
                }
            }

            return authorities;
        });

        return converter;
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
        String errorId = UUID.randomUUID().toString();
        logger.warn("Auth error (errorId={}) on path {}: {}", errorId, path, message);
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(
            "{"
                + "\"timestamp\":\"" + escapeJson(Instant.now().toString()) + "\"," 
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + escapeJson(status.getReasonPhrase()) + "\"," 
                + "\"message\":\"" + escapeJson(message) + "\"," 
                + "\"path\":\"" + escapeJson(path) + "\"," 
                + "\"errorId\":\"" + escapeJson(errorId) + "\""
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