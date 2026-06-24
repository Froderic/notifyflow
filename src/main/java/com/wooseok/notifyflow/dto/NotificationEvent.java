package com.wooseok.notifyflow.dto;

import java.time.Instant;
import java.util.UUID;

public sealed interface NotificationEvent
        permits UserSignupEvent, OrderPlacedEvent, PaymentReceivedEvent, PasswordResetEvent {

    UUID eventId();
    String eventType();
    String userId();
    Instant timestamp();
}