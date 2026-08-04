package com.flowforge.workflow.repository;

import com.flowforge.workflow.domain.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    List<Workflow> findByNextRunAtBefore(Instant now);
}
