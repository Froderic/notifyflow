package com.wooseok.notifyflow.service;

import com.wooseok.notifyflow.dto.NotificationEvent;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebhookSenderService {

    private final RestTemplate restTemplate;
    private final String mockWebhookUrl;

    public WebhookSenderService(RestTemplate restTemplate,
                                @org.springframework.beans.factory.annotation.Value(
                                        "${notifyflow.webhook.mock-url:http://localhost:8080/api/mock/webhook-receiver}")
                                String mockWebhookUrl) {
        this.restTemplate = restTemplate;
        this.mockWebhookUrl = mockWebhookUrl;
    }

    @Retryable(
            includes = RestClientException.class,
            maxRetries = 2,
            delay = 2000,
            multiplier = 2.0
    )
    public void send(NotificationEvent event) {
        System.out.println("[WEBHOOK] Attempting delivery for event " + event.eventId());
        restTemplate.postForObject(mockWebhookUrl, Map.of(
                "eventId", event.eventId(),
                "eventType", event.eventType(),
                "userId", event.userId()
        ), String.class);
        System.out.println("[WEBHOOK] Successfully delivered event " + event.eventId());
    }
}