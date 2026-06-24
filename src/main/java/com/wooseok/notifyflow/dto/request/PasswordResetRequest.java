package com.wooseok.notifyflow.dto.request;


public record PasswordResetRequest (
    String userId,
    String eventType,
    String resetToken
) implements EventPublishRequest {}