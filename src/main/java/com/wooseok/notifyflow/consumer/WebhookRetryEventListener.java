package com.wooseok.notifyflow.consumer;

import org.springframework.context.event.EventListener;
import org.springframework.resilience.retry.MethodRetryEvent;
import org.springframework.stereotype.Component;

@Component
public class WebhookRetryEventListener {

    private final WebhookDeliveryConsumer consumer;

    public WebhookRetryEventListener(WebhookDeliveryConsumer consumer) {
        this.consumer = consumer;
    }

    @EventListener
    public void onRetryEvent(MethodRetryEvent event) {
        if (event.isRetryAborted()) {
            System.out.println("[RETRY ABORTED] " + event.getMethod().getName()
                    + " | " + event.getFailure().getMessage());
        } else {
            consumer.incrementAttempt();
            System.out.println("[RETRY] Attempt failed for " + event.getMethod().getName()
                    + " | " + event.getFailure().getMessage());
        }
    }
}