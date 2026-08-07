package com.wooseok.notifyflow.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserSignupRequest(
        @NotBlank String userId,
        String eventType,
        @NotBlank String email,
        @NotBlank String signupSource
) implements EventPublishRequest {
}