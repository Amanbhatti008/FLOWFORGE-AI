package com.flowforge.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.api.response.StandardResponse;
import com.flowforge.security.entity.User;
import com.flowforge.security.repository.UserRepository;
import com.flowforge.workflow.domain.Task;
import com.flowforge.workflow.domain.WorkflowExecution;
import com.flowforge.workflow.dto.ExecutionStatusResponse;
import com.flowforge.workflow.dto.TaskStatusDto;
import com.flowforge.workflow.repository.TaskRepository;
import com.flowforge.workflow.repository.WorkflowExecutionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/executions")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ExecutionController {

    private final WorkflowExecutionRepository executionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<List<ExecutionStatusResponse>>> listExecutions(
            Authentication authentication,
            HttpServletRequest httpServletRequest) {
        
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WorkflowExecution> executions = executionRepository.findByUserIdOrderByStartedAtDesc(user.getId());

        List<ExecutionStatusResponse> response = executions.stream().map(exec -> 
            ExecutionStatusResponse.builder()
                .id(exec.getId())
                .workflowId(exec.getWorkflowVersion().getWorkflow().getId())
                .workflowName(exec.getWorkflowVersion().getWorkflow().getName())
                .versionNumber(exec.getWorkflowVersion().getVersionNumber())
                .status(exec.getStatus())
                .startedAt(exec.getStartedAt())
                .completedAt(exec.getCompletedAt())
                .tasks(List.of())
                .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(StandardResponse.success(response, "Executions retrieved successfully", traceId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<ExecutionStatusResponse>> getExecution(
            @PathVariable UUID id,
            HttpServletRequest httpServletRequest) {
        
        String traceId = (String) httpServletRequest.getAttribute("traceId");

        WorkflowExecution exec = executionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        List<Task> tasks = taskRepository.findByWorkflowExecutionId(id);

        List<TaskStatusDto> taskDtos = tasks.stream().map(t -> {
            String error = null;
            String aiDiagnosis = null;
            if (t.getOutputData() != null) {
                try {
                    JsonNode output = objectMapper.readTree(t.getOutputData());
                    if (output.has("error")) {
                        error = output.get("error").asText();
                    }
                    if (output.has("aiDiagnosis")) {
                        aiDiagnosis = output.get("aiDiagnosis").asText();
                    }
                } catch (Exception ignored) {}
            }
            return TaskStatusDto.builder()
                .id(t.getId())
                .taskRefName(t.getTaskRefName())
                .type(t.getType())
                .status(t.getStatus())
                .retryCount(t.getRetryCount())
                .startedAt(t.getStartedAt())
                .completedAt(t.getCompletedAt())
                .error(error)
                .aiDiagnosis(aiDiagnosis)
                .build();
        }).collect(Collectors.toList());

        ExecutionStatusResponse response = ExecutionStatusResponse.builder()
                .id(exec.getId())
                .workflowId(exec.getWorkflowVersion().getWorkflow().getId())
                .workflowName(exec.getWorkflowVersion().getWorkflow().getName())
                .versionNumber(exec.getWorkflowVersion().getVersionNumber())
                .status(exec.getStatus())
                .startedAt(exec.getStartedAt())
                .completedAt(exec.getCompletedAt())
                .tasks(taskDtos)
                .build();

        return ResponseEntity.ok(StandardResponse.success(response, "Execution retrieved successfully", traceId));
    }
}
