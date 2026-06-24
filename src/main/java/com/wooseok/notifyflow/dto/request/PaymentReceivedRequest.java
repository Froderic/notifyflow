package com.wooseok.notifyflow.dto.request;

import java.math.BigDecimal;

public record PaymentReceivedRequest (
    String userId,
    String eventType,
    String paymentId,
    BigDecimal amount
) implements EventPublishRequest {}