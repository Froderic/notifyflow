package com.wooseok.notifyflow.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventProducerService {

    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Counter eventsPublishedCounter;

    public EventProducerService(KafkaTemplate<String, Object> kafkaTemplate,
                                MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventsPublishedCounter = Counter.builder("notifyflow.events.published")
                .description("Total number of events published to Kafka")
                .register(meterRegistry);
    }

    public void publishEvent(String userId, Object eventPayload) {
        kafkaTemplate.send(TOPIC, userId, eventPayload);
        eventsPublishedCounter.increment();
    }
}