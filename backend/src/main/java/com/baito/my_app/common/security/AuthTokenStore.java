package com.baito.my_app.common.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed store for issued auth tokens. Keys are {@code auth:token:{token}} and hold the
 * encrypted principal payload; the entry's TTL is the token's lifetime. Deleting the key revokes
 * the token immediately (logout).
 */
@Component
public class AuthTokenStore {

    private static final String KEY_PREFIX = "auth:token:";

    private final StringRedisTemplate redisTemplate;

    public AuthTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void store(String token, String encryptedPayload, Duration ttl) {
        redisTemplate.opsForValue().set(key(token), encryptedPayload, ttl);
    }

    public Optional<String> find(String token) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(token)));
    }

    public void remove(String token) {
        redisTemplate.delete(key(token));
    }

    private static String key(String token) {
        return KEY_PREFIX + token;
    }
}
