package com.wooseok.notifyflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentReceivedRequest(
        @NotBlank String userId,
        String eventType,
        @NotBlank String paymentId,
        @NotNull @Positive BigDecimal amount
) implements EventPublishRequest {
}