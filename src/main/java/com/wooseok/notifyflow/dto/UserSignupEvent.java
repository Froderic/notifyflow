package com.wooseok.notifyflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record UserSignupEvent(
        UUID eventId,
        String userId,
        Instant timestamp,
        String email,
        String signupSource
) implements NotificationEvent {

    @Override
    @JsonProperty("eventType")
    public String eventType() {
        return "USER_SIGNUP";
    }
}