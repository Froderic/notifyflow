package com.wooseok.notifyflow;

import com.wooseok.notifyflow.service.EventDeduplicator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventDeduplicatorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventDeduplicator deduplicator;

    @Test
    void shouldAllowFirstOccurrenceOfEvent() {
        UUID eventId = UUID.randomUUID();
        assertThat(deduplicator.isDuplicate(eventId, "email")).isFalse();
    }

    @Test
    void shouldDetectDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        deduplicator.isDuplicate(eventId, "email"); // first occurrence
        assertThat(deduplicator.isDuplicate(eventId, "email")).isTrue();
    }

    @Test
    void shouldTrackNamespacesSeparately() {
        UUID eventId = UUID.randomUUID();
        deduplicator.isDuplicate(eventId, "email"); // mark as seen in email namespace
        // same eventId in webhook namespace should NOT be a duplicate
        assertThat(deduplicator.isDuplicate(eventId, "webhook")).isFalse();
    }

    @Test
    void shouldAllowDifferentEventIds() {
        UUID firstEvent = UUID.randomUUID();
        UUID secondEvent = UUID.randomUUID();
        deduplicator.isDuplicate(firstEvent, "audit");
        // different UUID — should not be considered a duplicate
        assertThat(deduplicator.isDuplicate(secondEvent, "audit")).isFalse();
    }
}