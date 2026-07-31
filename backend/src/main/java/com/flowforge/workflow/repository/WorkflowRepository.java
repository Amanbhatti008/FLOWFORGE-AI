package com.flowforge.workflow.repository;

import com.flowforge.workflow.domain.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
}
