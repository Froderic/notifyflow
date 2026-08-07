package com.wooseok.notifyflow.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.resilience.retry.MethodRetryEvent;
import org.springframework.stereotype.Component;

@Component
public class WebhookRetryEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebhookRetryEventListener.class);
    private final WebhookDeliveryConsumer consumer;

    public WebhookRetryEventListener(WebhookDeliveryConsumer consumer) {
        this.consumer = consumer;
    }

    @EventListener
    public void onRetryEvent(MethodRetryEvent event) {
        if (event.isRetryAborted()) {
            log.warn("Retry policy exhausted for method: {} | {}",
                    event.getMethod().getName(), event.getFailure().getMessage());
        } else {
            consumer.incrementAttempt();
            log.warn("Retry attempt failed for method: {} | {}",
                    event.getMethod().getName(), event.getFailure().getMessage());
        }
    }
}