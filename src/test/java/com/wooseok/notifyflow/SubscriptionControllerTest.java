package com.wooseok.notifyflow;

import com.wooseok.notifyflow.controller.SubscriptionController;
import com.wooseok.notifyflow.exception.GlobalExceptionHandler;
import com.wooseok.notifyflow.model.NotificationChannel;
import com.wooseok.notifyflow.model.Subscription;
import com.wooseok.notifyflow.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SubscriptionController.class, GlobalExceptionHandler.class})
class SubscriptionControllerTest {

    private static final String SUBSCRIBE_REQUEST = """
            {
                "userId": "user-123",
                "eventType": "ORDER_PLACED",
                "channel": "EMAIL"
            }
            """;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SubscriptionService subscriptionService;

    private Subscription mockSubscription(boolean active) {
        return new Subscription(
                "user-123", "ORDER_PLACED",
                NotificationChannel.EMAIL, active, Instant.now()
        );
    }

    @Test
    void shouldReturn200WhenSubscribeSucceeds() throws Exception {
        when(subscriptionService.subscribe(anyString(), anyString(), any()))
                .thenReturn(mockSubscription(true));

        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SUBSCRIBE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.eventType").value("ORDER_PLACED"));
    }

    @Test
    void shouldReturn200WhenUnsubscribeSucceeds() throws Exception {
        when(subscriptionService.unsubscribe(anyString(), anyString(), any()))
                .thenReturn(mockSubscription(false));

        mockMvc.perform(delete("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SUBSCRIBE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldReturn400WhenUnsubscribingNonexistentSubscription() throws Exception {
        when(subscriptionService.unsubscribe(anyString(), anyString(), any()))
                .thenThrow(new IllegalArgumentException(
                        "No subscription found for userId=user-123, eventType=ORDER_PLACED, channel=EMAIL"));

        mockMvc.perform(delete("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SUBSCRIBE_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "No subscription found for userId=user-123, eventType=ORDER_PLACED, channel=EMAIL"));
    }

    @Test
    void shouldReturn200WithActiveSubscriptionsList() throws Exception {
        when(subscriptionService.getActiveSubscriptions(anyString()))
                .thenReturn(List.of(mockSubscription(true)));

        mockMvc.perform(get("/api/subscriptions/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user-123"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void shouldReturn400WhenSubscribeRequestHasBlankUserId() throws Exception {
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "userId": "",
                                "eventType": "ORDER_PLACED",
                                "channel": "EMAIL"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}