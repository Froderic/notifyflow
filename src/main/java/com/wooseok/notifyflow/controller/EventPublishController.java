package com.wooseok.notifyflow.controller;

import com.wooseok.notifyflow.dto.*;
import com.wooseok.notifyflow.dto.request.*;
import com.wooseok.notifyflow.service.EventDeduplicator;
import com.wooseok.notifyflow.service.EventProducerService;
import com.wooseok.notifyflow.service.EventRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> publish(@Valid @RequestBody EventPublishRequest request,
                                     @Parameter(description = "Optional client-supplied idempotency key (UUID). " +
                                             "If provided, used as the eventId — duplicate submissions with the same key are rejected with 409.")
                                     @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        // Rate limiting check
        if (!rateLimiter.isAllowed(request.userId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ErrorResponse.of(429, "Too Many Requests",
                            "Rate limit exceeded. Max 10 requests per 60 seconds.",
                            "/api/events/publish"));
        }

        UUID eventId;
        if (idempotencyKey != null) {
            try {
                eventId = UUID.fromString(idempotencyKey);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ErrorResponse.of(400, "Bad Request",
                                "X-Idempotency-Key must be a valid UUID format (e.g. 123e4567-e89b-12d3-a456-426614174000)",
                                "/api/events/publish"));
            }
        } else {
            eventId = UUID.randomUUID();
        }

        NotificationEvent event = toEvent(request, eventId);

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

    private NotificationEvent toEvent(EventPublishRequest request, UUID eventId) {
        Instant now = Instant.now();

        return switch (request) {
            case UserSignupRequest r -> new UserSignupEvent(eventId, r.userId(), now, r.email(), r.signupSource());
            case OrderPlacedRequest r -> new OrderPlacedEvent(eventId, r.userId(), now, r.orderId(), r.orderTotal());
            case PaymentReceivedRequest r ->
                    new PaymentReceivedEvent(eventId, r.userId(), now, r.paymentId(), r.amount());
            case PasswordResetRequest r -> new PasswordResetEvent(eventId, r.userId(), now, r.resetToken());
        };
    }
}