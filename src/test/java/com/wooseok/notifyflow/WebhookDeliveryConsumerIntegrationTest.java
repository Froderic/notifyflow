package com.wooseok.notifyflow;

import com.wooseok.notifyflow.dto.*;
import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.repository.WebhookDeliveryLogRepository;
import com.wooseok.notifyflow.service.EventProducerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class WebhookDeliveryConsumerIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private EventProducerService producerService;

    @Autowired
    private WebhookDeliveryLogRepository webhookLogRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void resetFailureMode() {
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/mock/set-failure-mode?enabled=false",
                null, String.class);
    }

    @AfterEach
    void disableFailureMode() {
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/mock/set-failure-mode?enabled=false",
                null, String.class);
    }

    @Test
    void shouldDeliverWebhookSuccessfully() {
        // Explicitly ensure failure mode is off
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/mock/set-failure-mode?enabled=false",
                null, String.class);

        UUID eventId = UUID.randomUUID();
        OrderPlacedEvent event = new OrderPlacedEvent(
                eventId, "test-user-webhook-1", Instant.now(),
                "ord-webhook-1", BigDecimal.valueOf(99.99));

        producerService.publishEvent(event.userId(), event);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            var logs = webhookLogRepository.findAll();
            assertThat(logs).anyMatch(log ->
                    log.getEventId().equals(eventId) &&
                            log.getStatus() == DeliveryStatus.SENT
            );
        });
    }

    @Test
    void shouldMarkAsFailedAfterAllRetriesExhausted() {
        // Enable always-fail mode
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/mock/set-failure-mode?enabled=true",
                null, String.class);

        UUID eventId = UUID.randomUUID();
        PaymentReceivedEvent event = new PaymentReceivedEvent(
                eventId, "test-user-webhook-2", Instant.now(),
                "pay-webhook-fail", BigDecimal.valueOf(49.99));

        producerService.publishEvent(event.userId(), event);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            var logs = webhookLogRepository.findAll();
            assertThat(logs).anyMatch(log ->
                    log.getEventId().equals(eventId) &&
                            log.getStatus() == DeliveryStatus.FAILED
            );
        });
    }
}