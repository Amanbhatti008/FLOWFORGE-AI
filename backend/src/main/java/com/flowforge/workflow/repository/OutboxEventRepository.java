package com.flowforge.workflow.repository;

import com.flowforge.workflow.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = "SELECT * FROM outbox_events WHERE (status = 'PENDING' OR (status = 'FAILED' AND next_retry_at <= CURRENT_TIMESTAMP)) AND published = false ORDER BY created_at ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEvent> findRipeEvents(@Param("limit") int limit);
}
