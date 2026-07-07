package com.wooseok.notifyflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class MockWebhookController {

    private final AtomicInteger callCount = new AtomicInteger(0);

    @PostMapping("/api/mock/webhook-receiver")
    public ResponseEntity<String> receive(@RequestBody Object payload) {
        int count = callCount.incrementAndGet();
        System.out.println("[MOCK WEBHOOK] Received call #" + count + ": " + payload);

        // Simulate failure on every 3rd call, to exercise retry logic
        if (count % 3 == 0) {
            System.out.println("[MOCK WEBHOOK] Simulating failure for call #" + count);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Simulated failure");
        }

        return ResponseEntity.ok("Received");
    }

}