package com.wooseok.notifyflow.consumer;

import com.wooseok.notifyflow.dto.NotificationEvent;
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

    public DlqConsumer(DlqLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @KafkaListener(topics = "notification-events-dlq", groupId = "dlq-processor-group")
    public void consume(NotificationEvent event) {
        MDC.put("eventId", event.eventId().toString());
        MDC.put("eventType", event.eventType());
        try {
            log.info("Processing failed event: {} ({})", event.eventId(), event.eventType());

            // Simulated alert — in production this would page on-call, send to Slack, etc.
            log.warn("Permanent delivery failure for userId= {} ({})", event.userId(), event.eventType());

            repository.save(new DlqLog(
                    event.eventId(),
                    event.userId(),
                    event.eventType(),
                    "Webhook delivery failed after all retries",
                    Instant.now()
            ));
        } finally {
            MDC.clear();
        }
    }
}