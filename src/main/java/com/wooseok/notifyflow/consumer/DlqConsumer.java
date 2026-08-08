package com.wooseok.notifyflow.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wooseok.notifyflow.dto.NotificationEvent;
import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.model.DlqLog;
import com.wooseok.notifyflow.repository.DlqLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class DlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);
    private final DlqLogRepository repository;
    private final ObjectMapper objectMapper;

    public DlqConsumer(DlqLogRepository repository) {
        this.repository = repository;
        this.objectMapper = new ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Transactional
    @KafkaListener(topics = "notification-events-dlq", groupId = "dlq-processor-group")
    public void consume(NotificationEvent event) {
        MDC.put("eventId", event.eventId().toString());
        MDC.put("eventType", event.eventType());
        try {
            log.info("Processing failed event: {} ({})", event.eventId(), event.eventType());
            log.warn("Permanent delivery failure for userId= {} ({})", event.userId(), event.eventType());
            String payload = objectMapper.writeValueAsString(event);

            repository.save(new DlqLog(
                    event.eventId(),
                    event.userId(),
                    event.eventType(),
                    "Webhook delivery failed after all retries",
                    payload,
                    Instant.now(),
                    DeliveryStatus.FAILED
            ));

        } catch (Exception e) {
            log.error("Failed to process DLQ event: {}", e.getMessage());
        } finally {
            MDC.clear();
        }
    }
}