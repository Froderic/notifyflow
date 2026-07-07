package com.wooseok.notifyflow.consumer;

import com.wooseok.notifyflow.dto.NotificationEvent;
import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.model.WebhookDeliveryLog;
import com.wooseok.notifyflow.repository.WebhookDeliveryLogRepository;
import com.wooseok.notifyflow.service.DlqProducerService;
import com.wooseok.notifyflow.service.WebhookSenderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebhookDeliveryConsumer {

    private final WebhookSenderService senderService;
    private final WebhookDeliveryLogRepository repository;
    private final DlqProducerService dlqProducerService;
    private final ThreadLocal<AtomicInteger> attemptCounter =
            ThreadLocal.withInitial(AtomicInteger::new);

    public WebhookDeliveryConsumer(WebhookSenderService senderService,
                                   WebhookDeliveryLogRepository repository,
                                   DlqProducerService dlqProducerService) {
        this.senderService = senderService;
        this.repository = repository;
        this.dlqProducerService = dlqProducerService;
    }

    public int getAttemptCount() {
        return attemptCounter.get().get();
    }

    @KafkaListener(topics = "notification-events", groupId = "webhook-delivery-group")
    public void consume(NotificationEvent event) {
        attemptCounter.get().set(0);
        try {
            senderService.send(event);
            repository.save(new WebhookDeliveryLog(
                    event.eventId(), event.userId(), event.eventType(),
                    attemptCounter.get().get() + 1, DeliveryStatus.SENT, Instant.now()
            ));
        } catch (RestClientException ex) {
            int attempts = attemptCounter.get().get();
            System.out.println("[WEBHOOK] All retries exhausted after " + attempts
                    + " attempts for event " + event.eventId());
            repository.save(new WebhookDeliveryLog(
                    event.eventId(), event.userId(), event.eventType(),
                    attempts, DeliveryStatus.FAILED, Instant.now()
            ));
            dlqProducerService.publishToDlq(event, ex.getMessage());
        }
    }

    // Called by WebhookRetryEventListener to increment our counter on each retry
    public void incrementAttempt() {
        attemptCounter.get().incrementAndGet();
    }
}