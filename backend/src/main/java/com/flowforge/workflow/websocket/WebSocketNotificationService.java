package com.flowforge.workflow.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcasts a task status change to all subscribers of an execution.
     */
    public void notifyTaskStatusChange(UUID executionId, String taskRefName, String status) {
        String destination = "/topic/executions/" + executionId;
        Map<String, Object> payload = Map.of(
                "type", "TASK_STATUS_CHANGE",
                "taskRefName", taskRefName,
                "status", status,
                "timestamp", Instant.now().toString()
        );
        messagingTemplate.convertAndSend(destination, payload);
        log.debug("WebSocket: Sent task status change for {} -> {} on execution {}", taskRefName, status, executionId);
    }

    /**
     * Broadcasts a workflow execution status change.
     */
    public void notifyExecutionStatusChange(UUID executionId, String status) {
        String destination = "/topic/executions/" + executionId;
        Map<String, Object> payload = Map.of(
                "type", "EXECUTION_STATUS_CHANGE",
                "status", status,
                "timestamp", Instant.now().toString()
        );
        messagingTemplate.convertAndSend(destination, payload);
        log.info("WebSocket: Sent execution status change -> {} for {}", status, executionId);
    }
}
