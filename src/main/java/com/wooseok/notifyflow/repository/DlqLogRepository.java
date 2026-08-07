package com.wooseok.notifyflow.repository;

import com.wooseok.notifyflow.model.DlqLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DlqLogRepository extends JpaRepository<DlqLog, UUID> {
}