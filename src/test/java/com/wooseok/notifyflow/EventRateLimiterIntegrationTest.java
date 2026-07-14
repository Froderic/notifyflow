package com.wooseok.notifyflow;

import com.wooseok.notifyflow.service.EventRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class EventRateLimiterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventRateLimiter rateLimiter;

    @Test
    void shouldAllowRequestsUnderLimit() {
        for (int i = 0; i < 10; i++) {
            assertThat(rateLimiter.isAllowed("rate-test-user-1")).isTrue();
        }
    }

    @Test
    void shouldBlockRequestsOverLimit() {
        for (int i = 0; i < 10; i++) {
            rateLimiter.isAllowed("rate-test-user-2");
        }
        assertThat(rateLimiter.isAllowed("rate-test-user-2")).isFalse();
    }

    @Test
    void shouldTrackDifferentUsersSeparately() {
        for (int i = 0; i < 10; i++) {
            rateLimiter.isAllowed("rate-test-user-3");
        }
        // user-4 has its own independent counter — should still be allowed
        assertThat(rateLimiter.isAllowed("rate-test-user-4")).isTrue();
    }
}