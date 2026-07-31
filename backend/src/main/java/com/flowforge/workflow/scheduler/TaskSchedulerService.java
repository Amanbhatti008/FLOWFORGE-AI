package com.flowforge.workflow.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowforge.workflow.domain.OutboxEvent;
import com.flowforge.workflow.domain.Task;
import com.flowforge.workflow.repository.OutboxEventRepository;
import com.flowforge.workflow.repository.TaskRepository;
import com.flowforge.workflow.statemachine.OutboxStatus;
import com.flowforge.workflow.statemachine.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSchedulerService {

    private final TaskRepository taskRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Polls the database for SCHEDULED tasks that are ready to run.
     * Runs every 5 seconds.
     */
    @Scheduled(fixedDelayString = "5000")
    @Transactional
    public void pollAndQueueTasks() {
        // Fetch up to 100 ripe tasks per batch, locking them via FOR UPDATE SKIP LOCKED
        List<Task> ripeTasks = taskRepository.findRipeScheduledTasks(100);

        if (ripeTasks.isEmpty()) {
            return;
        }

        log.info("Found {} ripe SCHEDULED tasks. Transitioning to QUEUED and inserting into Outbox...", ripeTasks.size());

        for (Task task : ripeTasks) {
            try {
                // 1. Create the outbox payload
                ObjectNode payload = objectMapper.createObjectNode();
                payload.put("taskId", task.getId().toString());
                payload.put("workflowExecutionId", task.getWorkflowExecution().getId().toString());
                payload.put("type", task.getType());
                // Add input data if needed (as string)
                if (task.getInputData() != null) {
                    payload.set("inputData", objectMapper.readTree(task.getInputData()));
                }

                // 2. Create the OutboxEvent
                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setAggregateId(task.getId());
                outboxEvent.setEventType("TASK_EXECUTE");
                outboxEvent.setPayload(objectMapper.writeValueAsString(payload));
                outboxEvent.setStatus(OutboxStatus.PENDING);
                outboxEventRepository.save(outboxEvent);

                // 3. Transition Task to QUEUED
                task.setStatus(TaskStatus.QUEUED);
                taskRepository.save(task);

                log.debug("Successfully queued task {}", task.getId());
            } catch (Exception e) {
                log.error("Failed to queue task {}: {}", task.getId(), e.getMessage(), e);
                // Depending on error, we might leave it SCHEDULED to retry, or transition to FAILED.
                // For JSON parsing errors, we should fail it immediately to prevent poison pills.
                task.setStatus(TaskStatus.FAILED);
                taskRepository.save(task);
            }
        }
    }
}
