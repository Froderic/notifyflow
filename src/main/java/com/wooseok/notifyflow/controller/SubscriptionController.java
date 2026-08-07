package com.wooseok.notifyflow.controller;

import com.wooseok.notifyflow.dto.ErrorResponse;
import com.wooseok.notifyflow.model.NotificationChannel;
import com.wooseok.notifyflow.model.Subscription;
import com.wooseok.notifyflow.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscriptions", description = "Manages user notification subscriptions per event type and channel")
public class SubscriptionController {


    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Subscribe to notifications",
            description = "Creates or reactivates a subscription for a user to receive " +
                    "notifications for a specific event type via a given channel")
    @ApiResponse(responseCode = "200", description = "Subscription created or reactivated")
    public ResponseEntity<Subscription> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        Subscription sub = service.subscribe(
                request.userId(), request.eventType(),
                NotificationChannel.valueOf(request.channel().toUpperCase())
        );
        return ResponseEntity.ok(sub);
    }

    @DeleteMapping
    @Operation(summary = "Unsubscribe from notifications",
            description = "Deactivates an existing subscription. The subscription record is " +
                    "preserved for audit purposes with active=false")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription deactivated"),
            @ApiResponse(responseCode = "400", description = "Subscription not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Subscription> unsubscribe(@Valid @RequestBody SubscriptionRequest request) {
        Subscription sub = service.unsubscribe(
                request.userId(), request.eventType(),
                NotificationChannel.valueOf(request.channel().toUpperCase())
        );
        return ResponseEntity.ok(sub);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get active subscriptions",
            description = "Returns all active subscriptions for a given user across all event types and channels")
    @ApiResponse(responseCode = "200", description = "List of active subscriptions")
    public ResponseEntity<List<Subscription>> getSubscriptions(
            @Parameter(description = "The user ID to look up subscriptions for")
            @PathVariable String userId) {

        return ResponseEntity.ok(service.getActiveSubscriptions(userId));
    }

    public record SubscriptionRequest(
            @NotBlank String userId,
            String eventType,
            @NotBlank String channel
    ) {
    }
}