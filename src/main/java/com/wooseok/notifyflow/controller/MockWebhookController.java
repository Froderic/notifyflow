package com.wooseok.notifyflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class MockWebhookController {

    private final AtomicInteger callCount = new AtomicInteger(0);
    private volatile boolean alwaysFail = false;

    @PostMapping("/api/mock/webhook-receiver")
    public ResponseEntity<String> receive(@RequestBody Object payload) {
        int count = callCount.incrementAndGet();
        System.out.println("[MOCK WEBHOOK] Received call #" + count + ": " + payload);

        if (alwaysFail) {
            System.out.println("[MOCK WEBHOOK] Simulating failure for call #" + count);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Simulated failure");
        }

        return ResponseEntity.ok("Received");
    }

    @PostMapping("/api/mock/set-failure-mode")
    public ResponseEntity<String> setFailureMode(@RequestParam boolean enabled) {
        alwaysFail = enabled;
        callCount.set(0); // reset counter when mode changes
        System.out.println("[MOCK WEBHOOK] Failure mode set to: " + enabled);
        return ResponseEntity.ok("Failure mode: " + enabled);
    }
}