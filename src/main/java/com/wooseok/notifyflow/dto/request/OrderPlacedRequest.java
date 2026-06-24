package com.wooseok.notifyflow.dto.request;

import java.math.BigDecimal;

public record OrderPlacedRequest(
        String userId,
        String eventType,
        String orderId,
        BigDecimal orderTotal
) implements EventPublishRequest {}