package com.wooseok.notifyflow.dto.request;


public record UserSignupRequest (
    String userId,
    String eventType,
    String email,
    String signupSource
) implements EventPublishRequest {}