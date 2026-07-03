package com.wooseok.notifyflow.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class EventDeduplicator {

    private static final Duration DEDUP_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> redisTemplate;

    public EventDeduplicator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isDuplicate(UUID eventId) {
        String key = "dedup:" + eventId;

        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", DEDUP_TTL);

        // setIfAbsent returns true if key was set (new), false if already existed (duplicate)
        return Boolean.FALSE.equals(isNew);
    }
}