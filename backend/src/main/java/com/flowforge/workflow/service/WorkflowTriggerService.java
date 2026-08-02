package com.flowforge.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.api.exception.AuthenticationException;
import com.flowforge.security.entity.User;
import com.flowforge.security.repository.UserRepository;
import com.flowforge.workflow.dto.TriggerWorkflowRequest;
import com.flowforge.workflow.dto.WorkflowExecutionResponse;
import com.flowforge.workflow.domain.Task;
import com.flowforge.workflow.domain.WorkflowExecution;
import com.flowforge.workflow.domain.WorkflowVersion;
import com.flowforge.workflow.model.DagDefinition;

import com.flowforge.workflow.model.DagNode;
import com.flowforge.workflow.repository.TaskRepository;
import com.flowforge.workflow.repository.WorkflowExecutionRepository;
import com.flowforge.workflow.repository.WorkflowVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WorkflowTriggerService {

    private final WorkflowVersionRepository workflowVersionRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final com.flowforge.monitoring.MetricsService metricsService;

    @Transactional
    public WorkflowExecutionResponse triggerWorkflow(UUID workflowId, TriggerWorkflowRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        WorkflowVersion version;
        if (request.getWorkflowVersionId() != null) {
            version = workflowVersionRepository.findById(request.getWorkflowVersionId())
                    .orElseThrow(() -> new IllegalArgumentException("Workflow version not found"));
        } else {
            version = workflowVersionRepository.findLatestByWorkflowId(workflowId)
                    .orElseThrow(() -> new IllegalArgumentException("No versions found for this workflow"));
        }

        // 1. Create Workflow Execution record
        WorkflowExecution execution = new WorkflowExecution();
        execution.setWorkflowVersion(version);
        execution.setUser(user);
        execution.setStartedAt(Instant.now());
        execution.setStatus(com.flowforge.workflow.statemachine.WorkflowStatus.RUNNING);
        execution = workflowExecutionRepository.save(execution);
        
        metricsService.incrementWorkflowExecution();

        // 2. Parse DAG JSON
        try {
            DagDefinition dag = objectMapper.readValue(version.getDefinitionJson(), DagDefinition.class);
            
            // 3. Find initial nodes (nodes with no incoming edges) using DependencyResolver
            List<String> initialNodeIds = com.flowforge.workflow.dag.DependencyResolver.getInitialNodes(dag);

            if (initialNodeIds.isEmpty() && !dag.getNodes().isEmpty()) {
                throw new IllegalArgumentException("Invalid DAG: No initial nodes found (possible cycle)");
            }

            // 4. Create Initial Tasks
            String inputDataJson = request.getInputData() != null ? objectMapper.writeValueAsString(request.getInputData()) : null;

            for (String nodeId : initialNodeIds) {
                DagNode initialNode = dag.getNodes().stream().filter(n -> n.getId().equals(nodeId)).findFirst().orElseThrow();
                Task task = new Task();
                task.setWorkflowExecution(execution);
                task.setWorkflowVersion(version);
                task.setTaskRefName(initialNode.getId());
                task.setType(initialNode.getType());
                task.setStatus(com.flowforge.workflow.statemachine.TaskStatus.SCHEDULED);
                task.setInputData(inputDataJson); // Simple input propagation for now
                task.setScheduledAt(Instant.now());
                taskRepository.save(task);
            }
            
            // At this point we would publish an event to Kafka so workers can pick up SCHEDULED tasks
            // eventPublisher.publishExecutionCreatedEvent(...)

        } catch (Exception e) {
            throw new RuntimeException("Failed to trigger workflow execution: " + e.getMessage(), e);
        }

        return WorkflowExecutionResponse.builder()
                .executionId(execution.getId())
                .workflowId(workflowId)
                .workflowVersion(version.getVersionNumber())
                .status(execution.getStatus())
                .startedAt(execution.getStartedAt())
                .build();
    }
}
