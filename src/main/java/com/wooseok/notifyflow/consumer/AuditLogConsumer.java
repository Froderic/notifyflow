package com.wooseok.notifyflow.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wooseok.notifyflow.dto.NotificationEvent;
import com.wooseok.notifyflow.model.AuditLog;
import com.wooseok.notifyflow.repository.AuditLogRepository;
import com.wooseok.notifyflow.service.EventDeduplicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


@Component
public class AuditLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditLogConsumer.class);
    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    private final EventDeduplicator deduplicator;

    public AuditLogConsumer(AuditLogRepository repository,
                            EventDeduplicator deduplicator) {
        this.repository = repository;
        this.deduplicator = deduplicator;
    }

    @Transactional
    @KafkaListener(topics = "notification-events", groupId = "audit-log-group")
    public void consume(NotificationEvent event) {
        MDC.put("eventId", event.eventId().toString());
        MDC.put("eventType", event.eventType());
        try {
            if (deduplicator.isDuplicate(event.eventId(), "audit")) {
                log.info("Duplicate event detected, skipping");
                return;
            }

            String payloadJson = serialize(event);

            repository.save(new AuditLog(
                    event.eventId(), event.userId(), event.eventType(), payloadJson, Instant.now()
            ));

            log.info("Logged event {} ({})", event.eventId(), event.eventType());
        } finally {
            MDC.clear();
        }
    }

    private String serialize(NotificationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return "SERIALIZATION_FAILED: " + e.getMessage();
        }
    }
}