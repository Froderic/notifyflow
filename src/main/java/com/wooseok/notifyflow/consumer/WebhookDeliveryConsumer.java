package com.wooseok.notifyflow.consumer;

import com.wooseok.notifyflow.dto.NotificationEvent;
import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.model.WebhookDeliveryLog;
import com.wooseok.notifyflow.repository.WebhookDeliveryLogRepository;
import com.wooseok.notifyflow.service.DlqProducerService;
import com.wooseok.notifyflow.service.EventDeduplicator;
import com.wooseok.notifyflow.service.WebhookSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.transaction.annotation.Transactional;

@Component
public class WebhookDeliveryConsumer {

    private static final Logger log = LoggerFactory.getLogger(WebhookDeliveryConsumer.class);
    private final WebhookSenderService senderService;
    private final WebhookDeliveryLogRepository repository;
    private final DlqProducerService dlqProducerService;
    private final ThreadLocal<AtomicInteger> attemptCounter =
            ThreadLocal.withInitial(AtomicInteger::new);
    private final EventDeduplicator deduplicator;

    public WebhookDeliveryConsumer(WebhookSenderService senderService,
                                   WebhookDeliveryLogRepository repository,
                                   DlqProducerService dlqProducerService,
                                   EventDeduplicator deduplicator) {
        this.senderService = senderService;
        this.repository = repository;
        this.dlqProducerService = dlqProducerService;
        this.deduplicator = deduplicator;
    }

    public int getAttemptCount() {
        return attemptCounter.get().get();
    }

    @Transactional
    @KafkaListener(topics = "notification-events", groupId = "webhook-delivery-group")
    public void consume(NotificationEvent event) {
        MDC.put("eventId", event.eventId().toString());
        MDC.put("eventType", event.eventType());
        try {
            if (deduplicator.isDuplicate(event.eventId(), "webhook")) {
                log.info("Duplicate event detected, skipping");
                return;
            }

            attemptCounter.get().set(0);
            try {
                senderService.send(event);
                repository.save(new WebhookDeliveryLog(
                        event.eventId(), event.userId(), event.eventType(),
                        attemptCounter.get().get() + 1, DeliveryStatus.SENT, Instant.now()
                ));
            } catch (RestClientException ex) {
                int attempts = attemptCounter.get().get();
                log.warn("All retries exhausted after {} attempts", attempts);
                repository.save(new WebhookDeliveryLog(
                        event.eventId(), event.userId(), event.eventType(),
                        attempts, DeliveryStatus.FAILED, Instant.now()
                ));
                dlqProducerService.publishToDlq(event, ex.getMessage());
            }
        } finally {
            MDC.clear();
        }
    }

    // Called by WebhookRetryEventListener to increment our counter on each retry
    public void incrementAttempt() {
        attemptCounter.get().incrementAndGet();
    }
}