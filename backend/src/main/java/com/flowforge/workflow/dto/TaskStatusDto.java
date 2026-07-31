package com.flowforge.workflow.dto;

import com.flowforge.workflow.statemachine.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TaskStatusDto {
    private UUID id;
    private String taskRefName;
    private String type;
    private TaskStatus status;
    private Integer retryCount;
    private Instant startedAt;
    private Instant completedAt;
    private String error;
    private String aiDiagnosis;
}
