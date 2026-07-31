package com.flowforge.security.repository;

import com.flowforge.security.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    @Query(value = "SELECT current_hash FROM audit_logs ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastHash();
}
