package com.wooseok.notifyflow.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EventRateLimiter {

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final RedisTemplate<String, String> redisTemplate;

    public EventRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String userId) {
        String key = "rate:" + userId;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // First request in this window — set the expiry
            redisTemplate.expire(key, WINDOW);
        }

        return count <= MAX_REQUESTS;
    }
}