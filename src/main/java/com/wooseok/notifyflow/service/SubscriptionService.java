package com.wooseok.notifyflow.service;

import com.wooseok.notifyflow.model.NotificationChannel;
import com.wooseok.notifyflow.model.Subscription;
import com.wooseok.notifyflow.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository repository;

    public SubscriptionService(SubscriptionRepository repository) {
        this.repository = repository;
    }

    public Subscription subscribe(String userId, String eventType, NotificationChannel channel) {
        return repository.findByUserIdAndEventTypeAndChannel(userId, eventType, channel)
                .map(existing -> {
                    existing.setActive(true);
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(
                        new Subscription(userId, eventType, channel, true, Instant.now())
                ));
    }

    public Subscription unsubscribe(String userId, String eventType, NotificationChannel channel) {
        return repository.findByUserIdAndEventTypeAndChannel(userId, eventType, channel)
                .map(existing -> {
                    existing.setActive(false);
                    return repository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException(
                        "No subscription found for userId=" + userId +
                                ", eventType=" + eventType + ", channel=" + channel));
    }

    public List<Subscription> getActiveSubscriptions(String userId) {
        return repository.findByUserIdAndActiveTrue(userId);
    }
}
