package com.wooseok.notifyflow.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_delivery_log")
public class WebhookDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private int attemptCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false)
    private Instant deliveredAt;

    protected WebhookDeliveryLog() {
    }

    public WebhookDeliveryLog(UUID eventId, String userId, String eventType,
                              int attemptCount, DeliveryStatus status, Instant deliveredAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.attemptCount = attemptCount;
        this.status = status;
        this.deliveredAt = deliveredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }
}