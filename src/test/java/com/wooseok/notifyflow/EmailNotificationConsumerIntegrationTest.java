package com.wooseok.notifyflow;

import com.wooseok.notifyflow.dto.request.OrderPlacedRequest;
import com.wooseok.notifyflow.model.NotificationChannel;
import com.wooseok.notifyflow.repository.EmailNotificationLogRepository;
import com.wooseok.notifyflow.service.EventProducerService;
import com.wooseok.notifyflow.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class EmailNotificationConsumerIntegrationTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private EventProducerService producerService;

    @Autowired
    private EmailNotificationLogRepository emailLogRepository;

    @Test
    void shouldLogEmailWhenOrderPlacedEventReceived() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(), "test-user-email-1", Instant.now(),
                "order-123", BigDecimal.valueOf(99.99));

        producerService.publishEvent(event.userId(), event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var logs = emailLogRepository.findAll();
            assertThat(logs).anyMatch(log ->
                    log.getEventId().equals(event.eventId()) &&
                            log.getEventType().equals("ORDER_PLACED") &&
                            log.getSubject().contains("order-123")
            );
        });
    }

    @Test
    void shouldLogEmailForAllFourEventTypes() {
        UUID signupId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID resetId = UUID.randomUUID();

        producerService.publishEvent("test-user-email-2",
                new UserSignupEvent(signupId, "test-user-email-2", Instant.now(),
                        "test@example.com", "WEB"));
        producerService.publishEvent("test-user-email-2",
                new OrderPlacedEvent(orderId, "test-user-email-2", Instant.now(),
                        "ord-999", BigDecimal.TEN));
        producerService.publishEvent("test-user-email-2",
                new PaymentReceivedEvent(paymentId, "test-user-email-2", Instant.now(),
                        "pay-999", BigDecimal.TEN));
        producerService.publishEvent("test-user-email-2",
                new PasswordResetEvent(resetId, "test-user-email-2", Instant.now(),
                        "token-abc"));

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            var logs = emailLogRepository.findAll();
            assertThat(logs.stream().map(l -> l.getEventId()))
                    .contains(signupId, orderId, paymentId, resetId);
        });
    }
}