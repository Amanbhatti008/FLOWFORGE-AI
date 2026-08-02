package com.flowforge.workflow.dag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.workflow.domain.Task;
import com.flowforge.workflow.domain.WorkflowExecution;
import com.flowforge.workflow.model.DagDefinition;
import com.flowforge.workflow.model.DagNode;
import com.flowforge.workflow.repository.TaskRepository;
import com.flowforge.workflow.repository.WorkflowExecutionRepository;
import com.flowforge.workflow.statemachine.TaskStatus;
import com.flowforge.workflow.statemachine.WorkflowStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Called whenever a task successfully completes.
     * Evaluates the DAG to schedule next tasks or complete the workflow.
     */
    @Transactional
    public void evaluateProgression(WorkflowExecution execution) {
        try {
            // 1. Fetch all tasks for this execution
            List<Task> allTasks = taskRepository.findByWorkflowExecutionId(execution.getId());
            
            // 2. Parse DAG
            DagDefinition dag = objectMapper.readValue(execution.getWorkflowVersion().getDefinitionJson(), DagDefinition.class);
            
            // 3. Build sets of completed and scheduled/running tasks
            Set<String> completedNodes = allTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.SUCCESS)
                    .map(Task::getTaskRefName)
                    .collect(Collectors.toSet());
                    
            Set<String> inProgressNodes = allTasks.stream()
                    .filter(t -> t.getStatus() != TaskStatus.SUCCESS && t.getStatus() != TaskStatus.FAILED)
                    .map(Task::getTaskRefName)
                    .collect(Collectors.toSet());

            // 4. Check if workflow is complete
            if (completedNodes.size() == dag.getNodes().size()) {
                log.info("WorkflowExecution {} has completed all nodes successfully.", execution.getId());
                execution.setStatus(WorkflowStatus.SUCCESS);
                execution.setCompletedAt(Instant.now());
                workflowExecutionRepository.save(execution);
                
                long durationMillis = java.time.Duration.between(execution.getStartedAt(), execution.getCompletedAt()).toMillis();
                metricsService.recordWorkflowExecutionTime(durationMillis);
                
                webSocketNotificationService.notifyExecutionStatusChange(execution.getId(), "SUCCESS");
                return;
            }

            // 5. Calculate unlocked nodes
            List<String> unlockedNodes = DependencyResolver.getUnlockedNodes(dag, completedNodes, inProgressNodes);

            if (unlockedNodes.isEmpty()) {
                log.debug("WorkflowExecution {}: No new nodes unlocked at this time.", execution.getId());
                return;
            }

            log.info("WorkflowExecution {}: Unlocked new nodes: {}", execution.getId(), unlockedNodes);

            // 6. Schedule new tasks
            for (String nodeId : unlockedNodes) {
                DagNode nodeDef = dag.getNodes().stream().filter(n -> n.getId().equals(nodeId)).findFirst().orElseThrow();
                
                Task newTask = new Task();
                newTask.setWorkflowExecution(execution);
                newTask.setWorkflowVersion(execution.getWorkflowVersion());
                newTask.setTaskRefName(nodeDef.getId());
                newTask.setType(nodeDef.getType());
                newTask.setStatus(TaskStatus.SCHEDULED);
                newTask.setScheduledAt(Instant.now());
                
                taskRepository.save(newTask);
            }
        } catch (Exception e) {
            log.error("Failed to evaluate DAG progression for workflow execution {}: {}", execution.getId(), e.getMessage(), e);
            throw new RuntimeException("DAG Progression failed", e);
        }
    }
}
