package com.wooseok.notifyflow.controller;

import com.wooseok.notifyflow.model.NotificationChannel;
import com.wooseok.notifyflow.model.Subscription;
import com.wooseok.notifyflow.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Subscription> subscribe(@RequestBody SubscriptionRequest request) {
        Subscription sub = service.subscribe(
                request.userId(), request.eventType(),
                NotificationChannel.valueOf(request.channel().toUpperCase())
        );
        return ResponseEntity.ok(sub);
    }

    @DeleteMapping
    public ResponseEntity<Subscription> unsubscribe(@RequestBody SubscriptionRequest request) {
        Subscription sub = service.unsubscribe(
                request.userId(), request.eventType(),
                NotificationChannel.valueOf(request.channel().toUpperCase())
        );
        return ResponseEntity.ok(sub);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Subscription>> getSubscriptions(@PathVariable String userId) {
        return ResponseEntity.ok(service.getActiveSubscriptions(userId));
    }

    public record SubscriptionRequest(String userId, String eventType, String channel) {}
}