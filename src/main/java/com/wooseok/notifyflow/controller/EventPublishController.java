package com.wooseok.notifyflow.controller;

import com.wooseok.notifyflow.dto.*;
import com.wooseok.notifyflow.dto.request.*;
import com.wooseok.notifyflow.service.EventDeduplicator;
import com.wooseok.notifyflow.service.EventProducerService;
import com.wooseok.notifyflow.service.EventRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventPublishController {

    private final EventProducerService producerService;
    private final EventRateLimiter rateLimiter;
    private final EventDeduplicator deduplicator;

    public EventPublishController(EventProducerService producerService,
                                  EventRateLimiter rateLimiter,
                                  EventDeduplicator deduplicator) {
        this.producerService = producerService;
        this.rateLimiter = rateLimiter;
        this.deduplicator = deduplicator;
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody EventPublishRequest request) {
        // Rate limiting check
        if (!rateLimiter.isAllowed(request.userId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. Max " + 10 + " requests per 60 seconds.");
        }

        NotificationEvent event = toEvent(request);

        // Deduplication check
        if (deduplicator.isDuplicate(event.eventId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Duplicate event detected: " + event.eventId());
        }

        producerService.publishEvent(event.userId(), event);
        return ResponseEntity.accepted().build();
    }

    private NotificationEvent toEvent(EventPublishRequest request) {
        UUID eventId = UUID.randomUUID();
        Instant now = Instant.now();

        return switch (request) {
            case UserSignupRequest r -> new UserSignupEvent(eventId, r.userId(), now, r.email(), r.signupSource());
            case OrderPlacedRequest r -> new OrderPlacedEvent(eventId, r.userId(), now, r.orderId(), r.orderTotal());
            case PaymentReceivedRequest r -> new PaymentReceivedEvent(eventId, r.userId(), now, r.paymentId(), r.amount());
            case PasswordResetRequest r -> new PasswordResetEvent(eventId, r.userId(), now, r.resetToken());
        };
    }
}