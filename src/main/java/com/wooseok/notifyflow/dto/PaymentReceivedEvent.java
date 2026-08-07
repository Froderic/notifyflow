package com.wooseok.notifyflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentReceivedEvent(
        UUID eventId,
        String userId,
        Instant timestamp,
        String paymentId,
        BigDecimal amount
) implements NotificationEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "PAYMENT_RECEIVED";
    }
}
