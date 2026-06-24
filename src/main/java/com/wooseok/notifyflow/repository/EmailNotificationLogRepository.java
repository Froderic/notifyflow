package com.wooseok.notifyflow.repository;

import com.wooseok.notifyflow.model.EmailNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EmailNotificationLogRepository extends JpaRepository<EmailNotificationLog, UUID> {
}