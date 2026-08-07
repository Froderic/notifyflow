package com.wooseok.notifyflow;

import com.wooseok.notifyflow.dto.OrderPlacedEvent;
import com.wooseok.notifyflow.dto.PasswordResetEvent;
import com.wooseok.notifyflow.repository.AuditLogRepository;
import com.wooseok.notifyflow.service.EventProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class AuditLogConsumerIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private EventProducerService producerService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldLogEveryEventToAuditTable() {
        UUID eventId = UUID.randomUUID();
        OrderPlacedEvent event = new OrderPlacedEvent(
                eventId, "test-user-audit-1", Instant.now(),
                "ord-audit-1", BigDecimal.valueOf(49.99));

        producerService.publishEvent(event.userId(), event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var logs = auditLogRepository.findAll();
            assertThat(logs).anyMatch(log ->
                    log.getEventId().equals(eventId) &&
                            log.getEventType().equals("ORDER_PLACED")
            );
        });
    }

    @Test
    void shouldIncludeFullPayloadInAuditLog() {
        UUID eventId = UUID.randomUUID();
        PasswordResetEvent event = new PasswordResetEvent(
                eventId, "test-user-audit-2", Instant.now(), "reset-token-xyz");

        producerService.publishEvent(event.userId(), event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var logs = auditLogRepository.findAll();
            assertThat(logs).anyMatch(log ->
                    log.getEventId().equals(eventId) &&
                            log.getPayload().contains("reset-token-xyz")
            );
        });
    }
}