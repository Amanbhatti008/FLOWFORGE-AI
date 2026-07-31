package com.flowforge.workflow.repository;

import com.flowforge.workflow.domain.WorkflowExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import java.util.List;

public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {
    List<WorkflowExecution> findByUserIdOrderByStartedAtDesc(UUID userId);
}
