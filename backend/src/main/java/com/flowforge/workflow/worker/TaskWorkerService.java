package com.flowforge.workflow.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.workflow.dag.DagProgressionService;
import com.flowforge.workflow.domain.Task;
import com.flowforge.workflow.kafka.KafkaConfig;
import com.flowforge.workflow.repository.TaskRepository;
import com.flowforge.workflow.statemachine.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TaskWorkerService {

    private final TaskRepository taskRepository;
    private final DagProgressionService dagProgressionService;
    private final ObjectMapper objectMapper;
    private final java.util.List<TaskExecutor> taskExecutors;
    private final org.redisson.api.RedissonClient redissonClient;
    private final com.flowforge.workflow.websocket.WebSocketNotificationService webSocketNotificationService;
    private final com.flowforge.workflow.ai.AiService aiService;
    private static final int MAX_RETRIES = 3;

    @KafkaListener(topics = KafkaConfig.TOPIC_TASKS_EXECUTE, groupId = "flowforge-workers")
    @Transactional
    public void consumeTaskExecutionEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        log.info("Received execution event for partition {} offset {}", record.partition(), record.offset());
        
        try {
            JsonNode payload = objectMapper.readTree(record.value());
            UUID taskId = UUID.fromString(payload.get("taskId").asText());

            // DB Lock: PESSIMISTIC_WRITE ensures that if Kafka delivers duplicate messages,
            // the second thread blocks here until the first commits, then reads the updated status (e.g. RUNNING/SUCCESS)
            Task task = taskRepository.findByIdForUpdate(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

            if (task.getStatus() == TaskStatus.SUCCESS || task.getStatus() == TaskStatus.FAILED || task.getStatus() == TaskStatus.CANCELLED) {
                log.info("Task {} is already in terminal state {}, skipping", taskId, task.getStatus());
                acknowledgment.acknowledge();
                return;
            }

            // Distributed Lock: Redisson ensures at-most-once execution across horizontally scaled pods
            String lockKey = "flowforge:task-lock:" + taskId;
            org.redisson.api.RLock lock = redissonClient.getLock(lockKey);
            
            // Try to acquire lock for 5 seconds. If acquired, hold for 30 seconds (Watchdog will auto-extend if still running)
            boolean isLocked = lock.tryLock(5, 30, java.util.concurrent.TimeUnit.SECONDS);
            
            if (!isLocked) {
                log.warn("Could not acquire distributed lock for task {}. Another worker might be processing it.", taskId);
                return; // Do not acknowledge, let Kafka retry later if it actually crashed
            }

            try {
                task.setStatus(TaskStatus.RUNNING);
                task.setStartedAt(java.time.Instant.now());
                taskRepository.saveAndFlush(task);
                webSocketNotificationService.notifyTaskStatusChange(
                        task.getWorkflowExecution().getId(), task.getTaskRefName(), "RUNNING");
                
                log.info("Executing Task {} of type {}", task.getTaskRefName(), task.getType());
                
                TaskExecutor executor = taskExecutors.stream()
                        .filter(e -> e.getType().equals(task.getType()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No executor found for task type: " + task.getType()));
                
                boolean success = executor.execute(task);
                
                if (success) {
                    task.setStatus(TaskStatus.SUCCESS);
                    task.setCompletedAt(java.time.Instant.now());
                    taskRepository.save(task);
                    log.info("Task {} completed successfully.", task.getTaskRefName());
                    webSocketNotificationService.notifyTaskStatusChange(
                            task.getWorkflowExecution().getId(), task.getTaskRefName(), "SUCCESS");
                    dagProgressionService.evaluateProgression(task.getWorkflowExecution());
                } else {
                    handleTaskFailure(task);
                }

                acknowledgment.acknowledge();
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            
        } catch (Exception e) {
            log.error("Critical error processing task event: {}", e.getMessage(), e);
            acknowledgment.acknowledge(); 
        }
    }

    private void handleTaskFailure(Task task) {
        int currentRetries = task.getRetryCount() != null ? task.getRetryCount() : 0;
        if (currentRetries < MAX_RETRIES) {
            task.setRetryCount(currentRetries + 1);
            task.setStatus(TaskStatus.SCHEDULED); // put back to scheduled for scheduler to pick up
            // Exponential backoff: 5s, 10s, 20s
            int backoffSeconds = 5 * (int) Math.pow(2, currentRetries);
            task.setScheduledAt(java.time.Instant.now().plusSeconds(backoffSeconds));
            taskRepository.save(task);
            log.warn("Task {} failed. Scheduling retry {}/{} in {}s", task.getTaskRefName(), task.getRetryCount(), MAX_RETRIES, backoffSeconds);
        } else {
            task.setStatus(TaskStatus.FAILED);
            task.setCompletedAt(java.time.Instant.now());
            
            // Get AI Diagnosis
            String errorMsg = extractErrorMsg(task.getOutputData());
            String aiDiagnosis = aiService.analyzeFailure(task.getTaskRefName(), task.getType(), errorMsg, currentRetries);
            
            try {
                com.fasterxml.jackson.databind.node.ObjectNode outputNode;
                if (task.getOutputData() != null && !task.getOutputData().isBlank()) {
                    outputNode = (com.fasterxml.jackson.databind.node.ObjectNode) objectMapper.readTree(task.getOutputData());
                } else {
                    outputNode = objectMapper.createObjectNode();
                }
                outputNode.put("aiDiagnosis", aiDiagnosis);
                task.setOutputData(objectMapper.writeValueAsString(outputNode));
            } catch (Exception e) {
                log.error("Failed to append AI diagnosis to task output: {}", e.getMessage());
            }

            taskRepository.save(task);
            log.error("Task {} failed after {} retries. Terminal state.", task.getTaskRefName(), MAX_RETRIES);
            
            webSocketNotificationService.notifyTaskStatusChange(
                    task.getWorkflowExecution().getId(), task.getTaskRefName(), "FAILED");
            
            // Note: In a real system, dagProgressionService might fail the entire workflow here
        }
    }
    
    private String extractErrorMsg(String outputData) {
        if (outputData == null) return "Unknown error";
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(outputData);
            if (node.has("error")) return node.get("error").asText();
        } catch (Exception ignored) {}
        return "Unknown error";
    }
}
