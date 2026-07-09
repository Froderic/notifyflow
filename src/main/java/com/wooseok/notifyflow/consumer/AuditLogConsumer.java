package com.wooseok.notifyflow.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wooseok.notifyflow.dto.NotificationEvent;
import com.wooseok.notifyflow.model.AuditLog;
import com.wooseok.notifyflow.repository.AuditLogRepository;
import com.wooseok.notifyflow.service.EventDeduplicator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuditLogConsumer {

    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    private final EventDeduplicator deduplicator;

    public AuditLogConsumer(AuditLogRepository repository,
                            EventDeduplicator deduplicator) {
        this.repository = repository;
        this.deduplicator = deduplicator;
    }

    @KafkaListener(topics = "notification-events", groupId = "audit-log-group")
    public void consume(NotificationEvent event) {
        if (deduplicator.isDuplicate(event.eventId(), "audit")) {
            System.out.println("[AUDIT] Duplicate event detected, skipping: " + event.eventId());
            return;
        }

        String payloadJson = serialize(event);

        repository.save(new AuditLog(
                event.eventId(), event.userId(), event.eventType(), payloadJson, Instant.now()
        ));

        System.out.println("[AUDIT] Logged event " + event.eventId() + " (" + event.eventType() + ")");
    }

    private String serialize(NotificationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return "SERIALIZATION_FAILED: " + e.getMessage();
        }
    }
}