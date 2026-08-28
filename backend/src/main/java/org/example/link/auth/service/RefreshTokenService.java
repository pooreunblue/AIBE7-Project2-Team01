package org.example.link.auth.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.example.link.auth.config.AuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public void save(UUID userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + userId,
                refreshToken,
                authProperties.jwt().refreshTokenExpiry()
        );
    }

    public String get(UUID userId) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + userId);
    }

    public void delete(UUID userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}

