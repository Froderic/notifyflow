package com.wooseok.notifyflow.service;

import com.wooseok.notifyflow.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WebhookSenderService {

    private static final Logger log = LoggerFactory.getLogger(WebhookSenderService.class);
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
        MDC.put("eventId", event.eventId().toString());
        MDC.put("eventType", event.eventType());
        try {
            log.info("Attempting event delivery");
            restTemplate.postForObject(mockWebhookUrl, Map.of(
                    "eventId", event.eventId(),
                    "eventType", event.eventType(),
                    "userId", event.userId()
            ), String.class);
            log.info("Successfully delivered event");
        } finally {
            MDC.clear();
        }
    }
}