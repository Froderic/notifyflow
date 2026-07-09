package com.wooseok.notifyflow.consumer;

import com.wooseok.notifyflow.dto.NotificationEvent;
import com.wooseok.notifyflow.model.DlqLog;
import com.wooseok.notifyflow.repository.DlqLogRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DlqConsumer {

    private final DlqLogRepository repository;

    public DlqConsumer(DlqLogRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "notification-events-dlq", groupId = "dlq-processor-group")
    public void consume(NotificationEvent event) {
        System.out.println("[DLQ CONSUMER] Processing failed event: "
                + event.eventId() + " (" + event.eventType() + ")");

        // Simulated alert — in production this would page on-call, send to Slack, etc.
        System.out.println("[DLQ ALERT] Permanent delivery failure for userId="
                + event.userId() + " eventType=" + event.eventType());

        repository.save(new DlqLog(
                event.eventId(),
                event.userId(),
                event.eventType(),
                "Webhook delivery failed after all retries",
                Instant.now()
        ));
    }
}