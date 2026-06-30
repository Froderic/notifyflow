package com.wooseok.notifyflow.controller;

import com.wooseok.notifyflow.dto.*;
import com.wooseok.notifyflow.dto.request.*;
import com.wooseok.notifyflow.service.EventProducerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventPublishController {

    private final EventProducerService producerService;

    public EventPublishController(EventProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping("/publish")
    public ResponseEntity<Void> publish(@RequestBody EventPublishRequest request) {
        NotificationEvent event = toEvent(request);
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