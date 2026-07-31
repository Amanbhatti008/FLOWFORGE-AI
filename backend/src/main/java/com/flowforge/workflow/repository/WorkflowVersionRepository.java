package com.flowforge.workflow.repository;

import com.flowforge.workflow.domain.WorkflowVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WorkflowVersionRepository extends JpaRepository<WorkflowVersion, UUID> {
    
    @Query("SELECT wv FROM WorkflowVersion wv WHERE wv.workflow.id = :workflowId ORDER BY wv.versionNumber DESC LIMIT 1")
    Optional<WorkflowVersion> findLatestByWorkflowId(@Param("workflowId") UUID workflowId);
}
