package com.ceodashboard.backend.hrms.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenCacheService {

    private final Map<String, TokenCacheEntry> cache = new ConcurrentHashMap<>();

    public Optional<String> get(String key) {
        TokenCacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.token());
    }

    public void put(String key, String token, Instant expiresAt) {
        cache.put(key, new TokenCacheEntry(token, expiresAt));
    }

    public void evict(String key) {
        cache.remove(key);
    }

    private record TokenCacheEntry(String token, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
