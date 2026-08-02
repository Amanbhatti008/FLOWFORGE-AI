package com.flowforge.workflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.api.exception.AuthenticationException;
import com.flowforge.security.entity.User;
import com.flowforge.security.repository.UserRepository;
import com.flowforge.workflow.domain.Workflow;
import com.flowforge.workflow.domain.WorkflowVersion;
import com.flowforge.workflow.dto.CreateWorkflowRequest;
import com.flowforge.workflow.dto.CreateWorkflowVersionRequest;
import com.flowforge.workflow.dto.WorkflowResponse;
import com.flowforge.workflow.repository.WorkflowRepository;
import com.flowforge.workflow.repository.WorkflowVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowVersionRepository workflowVersionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WorkflowService workflowService;

    private User testUser;
    private Workflow testWorkflow;
    private UUID workflowId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@test.com");

        workflowId = UUID.randomUUID();
        testWorkflow = new Workflow();
        testWorkflow.setId(workflowId);
        testWorkflow.setName("Test Workflow");
        testWorkflow.setDescription("Test Description");
        testWorkflow.setCreatedBy(testUser);
        testWorkflow.setCreatedAt(Instant.now());
        testWorkflow.setUpdatedAt(Instant.now());
    }

    @Test
    void createWorkflow_Success() {
        CreateWorkflowRequest request = new CreateWorkflowRequest();
        request.setName("New Workflow");
        request.setDescription("New Description");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(workflowRepository.save(any(Workflow.class))).thenReturn(testWorkflow);

        WorkflowResponse response = workflowService.createWorkflow(request, "test@test.com");

        assertNotNull(response);
        assertEquals(testWorkflow.getId(), response.getId());
        assertEquals("Test Workflow", response.getName());
        assertEquals(0, response.getLatestVersion());
        verify(workflowRepository).save(any(Workflow.class));
    }

    @Test
    void createWorkflow_UserNotFound() {
        CreateWorkflowRequest request = new CreateWorkflowRequest();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> {
            workflowService.createWorkflow(request, "test@test.com");
        });
    }

    @Test
    void getWorkflow_Success() {
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(testWorkflow));
        
        WorkflowVersion version = new WorkflowVersion();
        version.setVersionNumber(2);
        when(workflowVersionRepository.findLatestByWorkflowId(workflowId)).thenReturn(Optional.of(version));

        WorkflowResponse response = workflowService.getWorkflow(workflowId);

        assertNotNull(response);
        assertEquals(workflowId, response.getId());
        assertEquals(2, response.getLatestVersion());
    }

    @Test
    void getWorkflow_NotFound() {
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            workflowService.getWorkflow(workflowId);
        });
    }

    @Test
    void listWorkflows_Success() {
        when(workflowRepository.findAll()).thenReturn(Arrays.asList(testWorkflow));
        when(workflowVersionRepository.findLatestByWorkflowId(workflowId)).thenReturn(Optional.empty());

        List<WorkflowResponse> responses = workflowService.listWorkflows();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(workflowId, responses.get(0).getId());
    }

    @Test
    void updateWorkflow_Success() {
        CreateWorkflowRequest request = new CreateWorkflowRequest();
        request.setName("Updated Workflow");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(testWorkflow));
        when(workflowRepository.save(any(Workflow.class))).thenReturn(testWorkflow);
        when(workflowVersionRepository.findLatestByWorkflowId(workflowId)).thenReturn(Optional.empty());

        WorkflowResponse response = workflowService.updateWorkflow(workflowId, request);

        assertNotNull(response);
        verify(workflowRepository).save(any(Workflow.class));
    }

    @Test
    void deleteWorkflow_Success() {
        when(workflowRepository.existsById(workflowId)).thenReturn(true);

        workflowService.deleteWorkflow(workflowId);

        verify(workflowRepository).deleteById(workflowId);
    }
}
