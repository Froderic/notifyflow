package com.wooseok.notifyflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wooseok.notifyflow.dto.NotificationEvent;
import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.model.DlqLog;
import com.wooseok.notifyflow.repository.DlqLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DlqRetryService {

    private static final Logger log = LoggerFactory.getLogger(DlqRetryService.class);

    private final DlqLogRepository dlqLogRepository;
    private final EventProducerService producerService;
    private final EventDeduplicator deduplicator;
    private final ObjectMapper objectMapper;

    public DlqRetryService(DlqLogRepository dlqLogRepository,
                           EventProducerService producerService,
                           EventDeduplicator deduplicator) {
        this.dlqLogRepository = dlqLogRepository;
        this.producerService = producerService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        this.deduplicator = deduplicator;
    }

    @Scheduled(fixedDelay = 3600000) // runs every hour
    @Transactional
    public void retryFailedEvents() {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        List<DlqLog> failedEvents = dlqLogRepository
                .findByStatusAndReceivedAtBefore(DeliveryStatus.FAILED, oneHourAgo);

        if (failedEvents.isEmpty()) {
            log.info("DLQ retry job: no failed events to retry");
            return;
        }

        log.warn("DLQ retry job: found {} failed events to retry", failedEvents.size());

        for (DlqLog dlqLog : failedEvents) {
            try {
                NotificationEvent event = objectMapper.readValue(
                        dlqLog.getPayload(), NotificationEvent.class);

                deduplicator.clearKey(event.eventId(), "webhook");

                producerService.publishEvent(dlqLog.getUserId(), event);
                dlqLog.setStatus(DeliveryStatus.SENT);
                dlqLogRepository.save(dlqLog);

                log.info("DLQ retry job: successfully re-published event {}",
                        dlqLog.getEventId());
            } catch (Exception e) {
                log.error("DLQ retry job: failed to re-publish event {}: {}",
                        dlqLog.getEventId(), e.getMessage());
            }
        }
    }
}