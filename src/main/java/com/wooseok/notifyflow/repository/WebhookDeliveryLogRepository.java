package com.wooseok.notifyflow.repository;

import com.wooseok.notifyflow.model.WebhookDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WebhookDeliveryLogRepository extends JpaRepository<WebhookDeliveryLog, UUID> {
}