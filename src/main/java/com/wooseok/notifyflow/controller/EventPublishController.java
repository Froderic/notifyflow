package com.wooseok.notifyflow.controller;

import com.wooseok.notifyflow.dto.*;
import com.wooseok.notifyflow.dto.request.*;
import com.wooseok.notifyflow.service.EventDeduplicator;
import com.wooseok.notifyflow.service.EventProducerService;
import com.wooseok.notifyflow.service.EventRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Publishing", description = "Publishes notification events into the Kafka pipeline")
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
    @Operation(
            summary = "Publish a notification event",
            description = "Accepts a typed notification event and publishes it to the Kafka " +
                    "notification-events topic. Supports USER_SIGNUP, ORDER_PLACED, " +
                    "PAYMENT_RECEIVED, and PASSWORD_RESET event types. " +
                    "Subject to rate limiting (10 requests/60s per user) and deduplication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Event accepted for async processing"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Duplicate event detected",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> publish(@RequestBody EventPublishRequest request) {
        // Rate limiting check
        if (!rateLimiter.isAllowed(request.userId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ErrorResponse.of(429, "Too Many Requests",
                            "Rate limit exceeded. Max 10 requests per 60 seconds.",
                            "/api/events/publish"));
        }

        NotificationEvent event = toEvent(request);

        // Deduplication check
        if (deduplicator.isDuplicate(event.eventId(), "publish")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.of(409, "Conflict",
                            "Duplicate event detected: " + event.eventId(),
                            "/api/events/publish"));
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