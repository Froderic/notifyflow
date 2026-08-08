package com.wooseok.notifyflow.repository;

import com.wooseok.notifyflow.model.DeliveryStatus;
import com.wooseok.notifyflow.model.DlqLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DlqLogRepository extends JpaRepository<DlqLog, UUID> {

    List<DlqLog> findByStatusAndReceivedAtBefore(DeliveryStatus status, Instant before);

}