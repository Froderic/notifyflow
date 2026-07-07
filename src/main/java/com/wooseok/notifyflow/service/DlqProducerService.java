package com.wooseok.notifyflow.service;

import com.wooseok.notifyflow.dto.NotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DlqProducerService {

    private static final String DLQ_TOPIC = "notification-events-dlq";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DlqProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishToDlq(NotificationEvent event, String failureReason) {
        System.out.println("[DLQ] Publishing failed event " + event.eventId()
                + " to DLQ. Reason: " + failureReason);
        kafkaTemplate.send(DLQ_TOPIC, event.userId(), event);
    }
}