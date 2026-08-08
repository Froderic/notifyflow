package com.wooseok.notifyflow.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dlq_log")
public class DlqLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String failureReason;

    @Column(nullable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    protected DlqLog() {
    }

    public DlqLog(UUID eventId, String userId, String eventType,
                  String failureReason, String payload, Instant receivedAt, DeliveryStatus status) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.failureReason = failureReason;
        this.payload = payload;
        this.receivedAt = receivedAt;
        this.status = status;
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

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public String getPayload() { return payload; }
}