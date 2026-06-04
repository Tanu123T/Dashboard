package com.ceodashboard.backend.hrms.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
public class KeycloakTokenManager {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakTokenManager.class);
    private static final String TOKEN_CACHE_KEY = "hrms-access-token";

    private final HrmsSecurityProperties securityProperties;
    private final TokenCacheService tokenCacheService;
    private final RestTemplate restTemplate;

    public KeycloakTokenManager(HrmsSecurityProperties securityProperties,
                                TokenCacheService tokenCacheService) {
        this.securityProperties = securityProperties;
        this.tokenCacheService = tokenCacheService;
        this.restTemplate = new RestTemplate();
    }

    public String getAccessToken() {
        return tokenCacheService.get(TOKEN_CACHE_KEY)
                .orElseGet(this::refreshToken);
    }

    private synchronized String refreshToken() {
        return tokenCacheService.get(TOKEN_CACHE_KEY)
                .orElseGet(() -> requestTokenFromKeycloak());
    }

    private String requestTokenFromKeycloak() {
        if (securityProperties.getToken().getEndpoint() == null || securityProperties.getToken().getEndpoint().isEmpty()) {
            logger.error("HRMS token endpoint is not configured");
            throw new IllegalStateException("HRMS token endpoint is not configured");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "password");
            form.add("client_id", securityProperties.getOauth().getClientId());
            form.add("username", securityProperties.getOauth().getUsername());
            form.add("password", securityProperties.getOauth().getPassword());
            form.add("scope", securityProperties.getOauth().getScope());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    securityProperties.getToken().getEndpoint(), entity, KeycloakTokenResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RestClientException("Failed to acquire HRMS access token: status=" + response.getStatusCodeValue());
            }

            KeycloakTokenResponse tokenResponse = response.getBody();
            String accessToken = tokenResponse.getAccessToken();
            Long expiresIn = tokenResponse.getExpiresIn();
            if (accessToken == null || accessToken.isBlank()) {
                throw new RestClientException("HRMS token response did not contain an access token");
            }

            Instant expiresAt = Instant.now().plusSeconds(expiresIn == null ? 300L : expiresIn - 30);
            tokenCacheService.put(TOKEN_CACHE_KEY, accessToken, expiresAt);
            logger.info("Acquired HRMS access token and cached until {}", expiresAt);
            return accessToken;
        } catch (Exception ex) {
            logger.error("Failed to retrieve HRMS access token", ex);
            tokenCacheService.evict(TOKEN_CACHE_KEY);
            throw new IllegalStateException("Failed to acquire HRMS access token", ex);
        }
    }
}
