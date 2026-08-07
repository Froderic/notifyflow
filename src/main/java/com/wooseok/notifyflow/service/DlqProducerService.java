package com.wooseok.notifyflow.service;

import com.wooseok.notifyflow.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DlqProducerService {

    private static final String DLQ_TOPIC = "notification-events-dlq";
    private static final Logger log = LoggerFactory.getLogger(DlqProducerService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DlqProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishToDlq(NotificationEvent event, String failureReason) {
        MDC.put("eventId", event.eventId().toString());
        MDC.put("eventType", event.eventType());
        try {
            log.info("Publishing failed event to DLQ. Reason: {}", failureReason);
            kafkaTemplate.send(DLQ_TOPIC, event.userId(), event);
        } finally {
            MDC.clear();
        }
    }
}