package com.flowforge.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.workflow.dto.CreateWorkflowRequest;
import com.flowforge.workflow.dto.WorkflowResponse;
import com.flowforge.workflow.service.WorkflowService;
import com.flowforge.workflow.service.WorkflowTriggerService;
import com.flowforge.workflow.controller.WorkflowController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class WorkflowControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private WorkflowService workflowService;

    @Mock
    private WorkflowTriggerService workflowTriggerService;

    @InjectMocks
    private WorkflowController workflowController;

    private UUID workflowId;
    private WorkflowResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(workflowController)
                .build();

        workflowId = UUID.randomUUID();
        mockResponse = new WorkflowResponse(workflowId, "Test Workflow", "Desc", null, null, 1);
    }

    @Test
    void testCreateWorkflow_Success() throws Exception {
        CreateWorkflowRequest request = new CreateWorkflowRequest();
        request.setName("Test Workflow");
        request.setDescription("Desc");

        when(workflowService.createWorkflow(any(CreateWorkflowRequest.class), eq("test@test.com"))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/workflows")
                .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("test@test.com", "password", java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(workflowId.toString()))
                .andExpect(jsonPath("$.data.name").value("Test Workflow"));
    }

    @Test
    void testGetWorkflow_Success() throws Exception {
        when(workflowService.getWorkflow(workflowId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/workflows/" + workflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Workflow"));
    }

    @Test
    void testListWorkflows_Success() throws Exception {
        when(workflowService.listWorkflows()).thenReturn(Collections.singletonList(mockResponse));

        mockMvc.perform(get("/api/v1/workflows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(workflowId.toString()));
    }
}
