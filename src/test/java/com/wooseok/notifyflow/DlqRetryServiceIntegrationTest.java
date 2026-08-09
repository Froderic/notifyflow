package com.wooseok.notifyflow;

import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.model.DlqLog;
import com.wooseok.notifyflow.repository.DlqLogRepository;
import com.wooseok.notifyflow.service.DlqRetryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class DlqRetryServiceIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private DlqRetryService dlqRetryService;

    @Autowired
    private DlqLogRepository dlqLogRepository;

    @Test
    void shouldRetryFailedEventsOlderThanOneHour() {
        UUID eventId = UUID.randomUUID();
        String payload = """
                {
                    "eventType": "ORDER_PLACED",
                    "eventId": "%s",
                    "userId": "retry-test-user",
                    "timestamp": "2026-08-01T00:00:00Z",
                    "orderId": "ord-retry-1",
                    "orderTotal": 49.99
                }
                """.formatted(eventId);

        DlqLog dlqLog = new DlqLog(
                eventId,
                "retry-test-user",
                "ORDER_PLACED",
                "Webhook delivery failed after all retries",
                payload,
                Instant.now().minus(2, ChronoUnit.HOURS),
                DeliveryStatus.FAILED
        );
        dlqLogRepository.save(dlqLog);

        dlqRetryService.retryFailedEvents();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            DlqLog updated = dlqLogRepository.findById(dlqLog.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(DeliveryStatus.SENT);
        });
    }

    @Test
    void shouldNotRetryEventsNewerThanOneHour() {
        UUID eventId = UUID.randomUUID();
        String payload = """
                {
                    "eventType": "PASSWORD_RESET",
                    "eventId": "%s",
                    "userId": "retry-test-user-2",
                    "timestamp": "2026-08-08T00:00:00Z",
                    "resetToken": "token-retry-test"
                }
                """.formatted(eventId);

        DlqLog dlqLog = new DlqLog(
                eventId,
                "retry-test-user-2",
                "PASSWORD_RESET",
                "Webhook delivery failed after all retries",
                payload,
                Instant.now(), // just now — should NOT be retried
                DeliveryStatus.FAILED
        );
        dlqLogRepository.save(dlqLog);

        dlqRetryService.retryFailedEvents();

        // Status should still be FAILED — too recent to retry
        DlqLog unchanged = dlqLogRepository.findById(dlqLog.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(DeliveryStatus.FAILED);
    }
}