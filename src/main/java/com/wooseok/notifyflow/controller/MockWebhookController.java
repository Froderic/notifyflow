package com.wooseok.notifyflow.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@Hidden
@RestController
public class MockWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MockWebhookController.class);
    private final AtomicInteger callCount = new AtomicInteger(0);
    private volatile boolean alwaysFail = false;

    @PostMapping("/api/mock/webhook-receiver")
    public ResponseEntity<String> receive(@RequestBody Object payload) {
        int count = callCount.incrementAndGet();
        log.info("Received call #{}: {}", count, payload);

        if (alwaysFail) {
            log.warn("Simulating failure for call #{}", count);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Simulated failure");
        }

        return ResponseEntity.ok("Received");
    }

    @PostMapping("/api/mock/set-failure-mode")
    public ResponseEntity<String> setFailureMode(@RequestParam boolean enabled) {
        alwaysFail = enabled;
        callCount.set(0);
        log.info("Failure mode set to: {}", enabled);
        return ResponseEntity.ok("Failure mode: " + enabled);
    }
}