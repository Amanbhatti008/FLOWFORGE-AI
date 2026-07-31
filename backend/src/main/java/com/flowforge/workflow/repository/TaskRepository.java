package com.flowforge.workflow.repository;

import com.flowforge.workflow.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByWorkflowExecutionId(UUID executionId);

    @Query(value = "SELECT * FROM tasks WHERE status = 'SCHEDULED' AND scheduled_at <= CURRENT_TIMESTAMP ORDER BY scheduled_at ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Task> findRipeScheduledTasks(@Param("limit") int limit);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Task t WHERE t.id = :id")
    java.util.Optional<Task> findByIdForUpdate(@Param("id") UUID id);
}
