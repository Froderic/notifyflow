package com.wooseok.notifyflow.consumer;

import com.wooseok.notifyflow.dto.*;
import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.model.EmailNotificationLog;
import com.wooseok.notifyflow.repository.EmailNotificationLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class EmailNotificationConsumer {

    private final EmailNotificationLogRepository repository;

    public EmailNotificationConsumer(EmailNotificationLogRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "notification-events", groupId = "email-notification-group")
    public void consume(NotificationEvent event) {
        String recipientEmail = event.userId() + "@example.com";
        String subject = buildSubject(event);

        // Simulated send — just logs to console + persists a record
        System.out.println("[EMAIL SIMULATED] To: " + recipientEmail + " | Subject: " + subject);

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