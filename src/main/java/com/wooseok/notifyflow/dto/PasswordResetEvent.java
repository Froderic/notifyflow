package com.wooseok.notifyflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetEvent(
        UUID eventId,
        String userId,
        Instant timestamp,
        String resetToken
) implements NotificationEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "PASSWORD_RESET";
    }
}