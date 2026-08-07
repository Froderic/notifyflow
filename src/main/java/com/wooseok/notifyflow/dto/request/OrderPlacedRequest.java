package com.wooseok.notifyflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderPlacedRequest(
        @NotBlank String userId,
        String eventType,
        @NotBlank String orderId,
        @NotNull @Positive BigDecimal orderTotal
) implements EventPublishRequest {
}