package com.wooseok.notifyflow.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_notification_log")
public class EmailNotificationLog {

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
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false)
    private Instant sentAt;

    protected EmailNotificationLog() {
    } // JPA requires no-arg constructor

    public EmailNotificationLog(UUID eventId, String userId, String eventType,
                                String recipientEmail, String subject,
                                DeliveryStatus status, Instant sentAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.status = status;
        this.sentAt = sentAt;
    }

    // Getters only — this is a log record, never updated after creation
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

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getSubject() {
        return subject;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}