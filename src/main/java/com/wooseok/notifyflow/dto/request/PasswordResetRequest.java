package com.wooseok.notifyflow.dto.request;

import jakarta.validation.constraints.NotBlank;


public record PasswordResetRequest(
        @NotBlank String userId,
        String eventType,
        @NotBlank String resetToken
) implements EventPublishRequest {
}