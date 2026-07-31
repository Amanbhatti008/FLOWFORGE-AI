package com.flowforge.security.repository;

import com.flowforge.security.entity.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActiveSessionRepository extends JpaRepository<ActiveSession, UUID> {
    List<ActiveSession> findByUserId(UUID userId);
    Optional<ActiveSession> findByUserIdAndDevice(UUID userId, String device);
    void deleteByUserIdAndDevice(UUID userId, String device);
    void deleteByUserId(UUID userId);
}
