package com.wooseok.notifyflow.consumer;

import com.wooseok.notifyflow.dto.*;
import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.model.EmailNotificationLog;
import com.wooseok.notifyflow.repository.EmailNotificationLogRepository;
import com.wooseok.notifyflow.service.EventDeduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class EmailNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationConsumer.class);
    private final EmailNotificationLogRepository repository;
    private final EventDeduplicator deduplicator;

    public EmailNotificationConsumer(EmailNotificationLogRepository repository,
                                     EventDeduplicator deduplicator) {
        this.repository = repository;
        this.deduplicator = deduplicator;
    }

    @Transactional
    @KafkaListener(topics = "notification-events", groupId = "email-notification-group")
    public void consume(NotificationEvent event) {
        MDC.put("eventId", event.eventId().toString());
        MDC.put("eventType", event.eventType());
        try {
            if (deduplicator.isDuplicate(event.eventId(), "email")) {
                log.info("Duplicate event detected, skipping");
                return;
            }

            String recipientEmail = event.userId() + "@example.com";
            String subject = buildSubject(event);

            // Simulated send — just logs to console + persists a record
            log.info("Simulated email sent to {} with subject: {}", recipientEmail, subject);

            EmailNotificationLog log = new EmailNotificationLog(
                    event.eventId(),
                    event.userId(),
                    event.eventType(),
                    recipientEmail,
                    subject,
                    DeliveryStatus.SENT,
                    Instant.now()
            );

            repository.save(log);
        } finally {
            MDC.clear();
        }
    }

    private String buildSubject(NotificationEvent event) {
        return switch (event) {
            case UserSignupEvent e -> "Welcome aboard, let's get you started!";
            case OrderPlacedEvent e -> "Your order " + e.orderId() + " has been placed";
            case PaymentReceivedEvent e -> "Payment received for " + e.paymentId();
            case PasswordResetEvent e -> "Reset your password";
        };
    }
}