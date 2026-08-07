package com.wooseok.notifyflow.repository;

import com.wooseok.notifyflow.model.NotificationChannel;
import com.wooseok.notifyflow.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserIdAndActiveTrue(String userId);

    Optional<Subscription> findByUserIdAndEventTypeAndChannel(
            String userId, String eventType, NotificationChannel channel);
}