package com.wooseok.notifyflow;

import com.wooseok.notifyflow.dto.*;
import com.wooseok.notifyflow.repository.DlqLogRepository;
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

class DlqConsumerIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private EventProducerService producerService;

    @Autowired
    private DlqLogRepository dlqLogRepository;

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
    void shouldPublishToDlqAfterAllRetriesExhausted() {
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/mock/set-failure-mode?enabled=true",
                null, String.class);

        UUID eventId = UUID.randomUUID();
        PasswordResetEvent event = new PasswordResetEvent(
                eventId, "test-user-dlq-1", Instant.now(), "dlq-reset-token");

        producerService.publishEvent(event.userId(), event);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            var logs = dlqLogRepository.findAll();
            assertThat(logs).anyMatch(log ->
                    log.getEventId().equals(eventId) &&
                            log.getEventType().equals("PASSWORD_RESET")
            );
        });
    }
}