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

    public boolean isDuplicate(UUID eventId, String namespace) {
        String key = "dedup:" + namespace + ":" + eventId;
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", DEDUP_TTL);
        return Boolean.FALSE.equals(isNew);
    }

    public void clearKey(UUID eventId, String namespace) {
        String key = "dedup:" + namespace + ":" + eventId;
        redisTemplate.delete(key);
    }

}