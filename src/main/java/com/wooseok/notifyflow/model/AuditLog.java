package com.wooseok.notifyflow.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
public class AuditLog {

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
    private String payload;

    @Column(nullable = false)
    private Instant recordedAt;

    protected AuditLog() {}

    public AuditLog(UUID eventId, String userId, String eventType, String payload, Instant recordedAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.payload = payload;
        this.recordedAt = recordedAt;
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getUserId() { return userId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public Instant getRecordedAt() { return recordedAt; }
}