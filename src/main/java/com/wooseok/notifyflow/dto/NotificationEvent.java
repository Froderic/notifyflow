package com.wooseok.notifyflow.dto;

import java.time.Instant;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserSignupEvent.class, name = "USER_SIGNUP"),
        @JsonSubTypes.Type(value = OrderPlacedEvent.class, name = "ORDER_PLACED"),
        @JsonSubTypes.Type(value = PaymentReceivedEvent.class, name = "PAYMENT_RECEIVED"),
        @JsonSubTypes.Type(value = PasswordResetEvent.class, name = "PASSWORD_RESET")
})

public sealed interface NotificationEvent
        permits UserSignupEvent, OrderPlacedEvent, PaymentReceivedEvent, PasswordResetEvent {

    UUID eventId();

    String eventType();

    String userId();

    Instant timestamp();
}