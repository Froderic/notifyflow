package com.wooseok.notifyflow;

import com.wooseok.notifyflow.controller.EventPublishController;
import com.wooseok.notifyflow.exception.GlobalExceptionHandler;
import com.wooseok.notifyflow.service.DlqProducerService;
import com.wooseok.notifyflow.service.EventDeduplicator;
import com.wooseok.notifyflow.service.EventProducerService;
import com.wooseok.notifyflow.service.EventRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {EventPublishController.class, GlobalExceptionHandler.class})
class EventPublishControllerTest {

    private static final String VALID_ORDER_PLACED = """
            {
                "eventType": "ORDER_PLACED",
                "userId": "user-123",
                "orderId": "ord-456",
                "orderTotal": 49.99
            }
            """;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private EventProducerService producerService;
    @MockitoBean
    private EventRateLimiter rateLimiter;
    @MockitoBean
    private EventDeduplicator deduplicator;
    @MockitoBean
    private DlqProducerService dlqProducerService;

    @Test
    void shouldReturn202WhenEventPublishedSuccessfully() throws Exception {
        when(rateLimiter.isAllowed(anyString())).thenReturn(true);
        when(deduplicator.isDuplicate(any(UUID.class), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/events/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER_PLACED))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturn429WhenRateLimitExceeded() throws Exception {
        when(rateLimiter.isAllowed(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/events/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER_PLACED))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn409WhenDuplicateEventDetected() throws Exception {
        when(rateLimiter.isAllowed(anyString())).thenReturn(true);
        when(deduplicator.isDuplicate(any(UUID.class), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/events/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_ORDER_PLACED))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturn400WhenEventTypeIsMissing() throws Exception {
        mockMvc.perform(post("/api/events/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": "user-123",
                                    "orderId": "ord-456",
                                    "orderTotal": 49.99
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenUserIdIsBlank() throws Exception {
        when(rateLimiter.isAllowed(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/events/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "ORDER_PLACED",
                                "userId": "",
                                "orderId": "ord-456",
                                "orderTotal": 49.99
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("userId: must not be blank"));
    }

    @Test
    void shouldReturn400WhenOrderTotalIsNegative() throws Exception {
        when(rateLimiter.isAllowed(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/events/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "eventType": "ORDER_PLACED",
                                "userId": "user-123",
                                "orderId": "ord-456",
                                "orderTotal": -10.00
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("orderTotal: must be greater than 0"));
    }

    @Test
    void shouldReturn400WhenIdempotencyKeyIsInvalidFormat() throws Exception {
        when(rateLimiter.isAllowed(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/events/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Idempotency-Key", "not-a-valid-uuid")
                        .content(VALID_ORDER_PLACED))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }
}