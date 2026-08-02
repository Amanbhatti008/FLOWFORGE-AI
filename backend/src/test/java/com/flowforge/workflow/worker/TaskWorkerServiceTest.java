package com.flowforge.workflow.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowforge.workflow.dag.DagProgressionService;
import com.flowforge.workflow.domain.Task;
import com.flowforge.workflow.domain.WorkflowExecution;
import com.flowforge.workflow.repository.TaskRepository;
import com.flowforge.workflow.statemachine.TaskStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class TaskWorkerServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private DagProgressionService dagProgressionService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private com.flowforge.workflow.websocket.WebSocketNotificationService webSocketNotificationService;

    @Mock
    private com.flowforge.workflow.ai.AiService aiService;

    @Mock
    private TaskExecutor taskExecutor;

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private RLock lock;

    @InjectMocks
    private TaskWorkerService taskWorkerService;

    private Task testTask;
    private UUID taskId;
    private String jsonPayload;

    @BeforeEach
    void setUp() throws Exception {
        taskId = UUID.randomUUID();
        testTask = new Task();
        testTask.setId(taskId);
        testTask.setTaskRefName("task_1");
        testTask.setType("HTTP");
        testTask.setStatus(TaskStatus.SCHEDULED);
        WorkflowExecution execution = new WorkflowExecution();
        execution.setId(UUID.randomUUID());
        testTask.setWorkflowExecution(execution);

        List<TaskExecutor> executors = new ArrayList<>();
        executors.add(taskExecutor);
        ReflectionTestUtils.setField(taskWorkerService, "taskExecutors", executors);

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("taskId", taskId.toString());
        jsonPayload = objectMapper.writeValueAsString(payload);
    }

    @Test
    void consumeTaskExecutionEvent_Success() throws Exception {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", jsonPayload);

        when(taskRepository.findByIdForUpdate(taskId)).thenReturn(Optional.of(testTask));
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        
        when(taskExecutor.getType()).thenReturn("HTTP");
        when(taskExecutor.execute(any(Task.class))).thenReturn(true);

        taskWorkerService.consumeTaskExecutionEvent(record, acknowledgment);

        verify(taskRepository).save(any(Task.class));
        verify(webSocketNotificationService, times(2)).notifyTaskStatusChange(any(UUID.class), eq("task_1"), anyString());
        verify(dagProgressionService).evaluateProgression(any(WorkflowExecution.class));
        verify(acknowledgment).acknowledge();
        verify(lock).unlock();
    }

    @Test
    void consumeTaskExecutionEvent_TaskAlreadyTerminal() throws Exception {
        testTask.setStatus(TaskStatus.SUCCESS);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", jsonPayload);

        when(taskRepository.findByIdForUpdate(taskId)).thenReturn(Optional.of(testTask));

        taskWorkerService.consumeTaskExecutionEvent(record, acknowledgment);

        verify(acknowledgment).acknowledge();
        verifyNoInteractions(redissonClient);
        verifyNoInteractions(taskExecutor);
    }

    @Test
    void consumeTaskExecutionEvent_LockNotAcquired() throws Exception {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", jsonPayload);

        when(taskRepository.findByIdForUpdate(taskId)).thenReturn(Optional.of(testTask));
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        taskWorkerService.consumeTaskExecutionEvent(record, acknowledgment);

        // Should not acknowledge if lock not acquired
        verify(acknowledgment, never()).acknowledge();
        verifyNoInteractions(taskExecutor);
    }
}
