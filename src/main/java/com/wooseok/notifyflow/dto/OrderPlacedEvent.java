package com.wooseok.notifyflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;
import java.math.BigDecimal;

public record OrderPlacedEvent(
        UUID eventId,
        String userId,
        Instant timestamp,
        String orderId,
        BigDecimal orderTotal
) implements NotificationEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "ORDER_PLACED";
    }
}