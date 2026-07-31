package com.flowforge.workflow.dto;

import com.flowforge.workflow.statemachine.WorkflowStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ExecutionStatusResponse {
    private UUID id;
    private UUID workflowId;
    private String workflowName;
    private Integer versionNumber;
    private WorkflowStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private List<TaskStatusDto> tasks;
}
