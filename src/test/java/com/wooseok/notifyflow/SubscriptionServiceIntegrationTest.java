package com.wooseok.notifyflow;

import com.wooseok.notifyflow.model.NotificationChannel;
import com.wooseok.notifyflow.model.Subscription;
import com.wooseok.notifyflow.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SubscriptionService subscriptionService;

    @Test
    void shouldSubscribeUserToEventType() {
        Subscription sub = subscriptionService.subscribe(
                "user-test-1", "ORDER_PLACED", NotificationChannel.EMAIL);

        assertThat(sub.getId()).isNotNull();
        assertThat(sub.getUserId()).isEqualTo("user-test-1");
        assertThat(sub.getEventType()).isEqualTo("ORDER_PLACED");
        assertThat(sub.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(sub.isActive()).isTrue();
    }

    @Test
    void shouldUnsubscribeUser() {
        subscriptionService.subscribe("user-test-2", "PAYMENT_RECEIVED", NotificationChannel.WEBHOOK);
        Subscription sub = subscriptionService.unsubscribe(
                "user-test-2", "PAYMENT_RECEIVED", NotificationChannel.WEBHOOK);

        assertThat(sub.isActive()).isFalse();
    }

    @Test
    void shouldReturnOnlyActiveSubscriptions() {
        subscriptionService.subscribe("user-test-3", "USER_SIGNUP", NotificationChannel.EMAIL);
        subscriptionService.subscribe("user-test-3", "PASSWORD_RESET", NotificationChannel.EMAIL);
        subscriptionService.unsubscribe("user-test-3", "PASSWORD_RESET", NotificationChannel.EMAIL);

        List<Subscription> active = subscriptionService.getActiveSubscriptions("user-test-3");

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getEventType()).isEqualTo("USER_SIGNUP");
    }

    @Test
    void shouldUpsertExistingSubscription() {
        subscriptionService.subscribe("user-test-4", "ORDER_PLACED", NotificationChannel.EMAIL);
        subscriptionService.unsubscribe("user-test-4", "ORDER_PLACED", NotificationChannel.EMAIL);
        Subscription resubscribed = subscriptionService.subscribe(
                "user-test-4", "ORDER_PLACED", NotificationChannel.EMAIL);

        assertThat(resubscribed.isActive()).isTrue();
    }
}