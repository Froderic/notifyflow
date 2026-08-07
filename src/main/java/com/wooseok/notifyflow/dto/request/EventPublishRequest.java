package com.wooseok.notifyflow.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserSignupRequest.class, name = "USER_SIGNUP"),
        @JsonSubTypes.Type(value = OrderPlacedRequest.class, name = "ORDER_PLACED"),
        @JsonSubTypes.Type(value = PaymentReceivedRequest.class, name = "PAYMENT_RECEIVED"),
        @JsonSubTypes.Type(value = PasswordResetRequest.class, name = "PASSWORD_RESET")
})
public sealed interface EventPublishRequest
        permits UserSignupRequest, OrderPlacedRequest, PaymentReceivedRequest, PasswordResetRequest {

    String userId();

    String eventType();
}