package com.flowforge.workflow.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class WorkflowExecutionResponse {
    private UUID executionId;
    private UUID workflowId;
    private Integer workflowVersion;
    private com.flowforge.workflow.statemachine.WorkflowStatus status;
    private Instant startedAt;
    private Instant completedAt;
}
