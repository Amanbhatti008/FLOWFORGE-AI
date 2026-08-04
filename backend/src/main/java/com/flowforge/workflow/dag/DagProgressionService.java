package com.flowforge.workflow.dag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.workflow.domain.Task;
import com.flowforge.workflow.domain.WorkflowExecution;
import com.flowforge.workflow.model.DagDefinition;
import com.flowforge.workflow.model.DagEdge;
import com.flowforge.workflow.model.DagNode;
import com.flowforge.workflow.repository.TaskRepository;
import com.flowforge.workflow.repository.WorkflowExecutionRepository;
import com.flowforge.workflow.statemachine.TaskStatus;
import com.flowforge.workflow.statemachine.WorkflowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DagProgressionService {

    private final TaskRepository taskRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final ObjectMapper objectMapper;
    private final com.flowforge.workflow.websocket.WebSocketNotificationService webSocketNotificationService;
    private final com.flowforge.monitoring.MetricsService metricsService;
    private final ApplicationEventPublisher eventPublisher;
    private final ConditionEvaluator conditionEvaluator;

    @Transactional
    public void evaluateProgression(WorkflowExecution execution) {
        try {
            List<Task> allTasks = taskRepository.findByWorkflowExecutionId(execution.getId());
            DagDefinition dag = objectMapper.readValue(execution.getWorkflowVersion().getDefinitionJson(), DagDefinition.class);
            
            Set<String> completedNodes = allTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.SUCCESS)
                    .map(Task::getTaskRefName)
                    .collect(Collectors.toSet());
                    
            Set<String> skippedNodes = allTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.SKIPPED)
                    .map(Task::getTaskRefName)
                    .collect(Collectors.toSet());
                    
            Set<String> inProgressNodes = allTasks.stream()
                    .filter(t -> t.getStatus() != TaskStatus.SUCCESS && t.getStatus() != TaskStatus.FAILED && t.getStatus() != TaskStatus.SKIPPED)
                    .map(Task::getTaskRefName)
                    .collect(Collectors.toSet());

            if (completedNodes.size() + skippedNodes.size() == dag.getNodes().size()) {
                log.info("WorkflowExecution {} has completed all nodes.", execution.getId());
                execution.setStatus(WorkflowStatus.SUCCESS);
                execution.setCompletedAt(Instant.now());
                workflowExecutionRepository.save(execution);
                
                long durationMillis = java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis();
                metricsService.recordWorkflowExecutionTime(durationMillis);
                webSocketNotificationService.notifyExecutionStatusChange(execution.getId(), "SUCCESS");
                return;
            }

            List<String> unlockedNodes = DependencyResolver.getUnlockedNodes(dag, completedNodes, skippedNodes, inProgressNodes);

            if (unlockedNodes.isEmpty()) {
                return;
            }

            boolean anySkipped = false;

            for (String nodeId : unlockedNodes) {
                DagNode nodeDef = dag.getNodes().stream().filter(n -> n.getId().equals(nodeId)).findFirst().orElseThrow();
                
                boolean shouldSkip = false;
                if (dag.getEdges() != null) {
                    for (DagEdge edge : dag.getEdges()) {
                        if (edge.getTarget().equals(nodeId)) {
                            if (skippedNodes.contains(edge.getSource())) {
                                shouldSkip = true;
                                break;
                            } else if (completedNodes.contains(edge.getSource()) && edge.getCondition() != null && !edge.getCondition().trim().isEmpty()) {
                                Task sourceTask = allTasks.stream().filter(t -> t.getTaskRefName().equals(edge.getSource())).findFirst().orElse(null);
                                if (sourceTask != null && sourceTask.getOutputData() != null) {
                                    boolean conditionMet = conditionEvaluator.evaluate(edge.getCondition(), sourceTask.getOutputData());
                                    if (!conditionMet) {
                                        shouldSkip = true;
                                        break;
                                    }
                                } else {
                                    // If there's a condition but no output, we cannot satisfy the condition
                                    shouldSkip = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                Task newTask = new Task();
                newTask.setWorkflowExecution(execution);
                newTask.setWorkflowVersion(execution.getWorkflowVersion());
                newTask.setTaskRefName(nodeDef.getId());
                newTask.setType(nodeDef.getType());
                newTask.setStatus(shouldSkip ? TaskStatus.SKIPPED : TaskStatus.SCHEDULED);
                newTask.setScheduledAt(Instant.now());
                if (shouldSkip) newTask.setCompletedAt(Instant.now());
                
                taskRepository.save(newTask);
                
                if (shouldSkip) {
                    anySkipped = true;
                } else {
                    String eventPayload = String.format("{\"taskId\": \"%s\"}", newTask.getId().toString());
                    eventPublisher.publishEvent(eventPayload);
                }
            }

            // If we skipped nodes, their downstream nodes might now be unlocked. Evaluate recursively.
            if (anySkipped) {
                evaluateProgression(execution);
            }

        } catch (Exception e) {
            log.error("Failed to evaluate DAG progression for workflow execution {}: {}", execution.getId(), e.getMessage(), e);
            throw new RuntimeException("DAG Progression failed", e);
        }
    }
}
