package com.flowforge.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.security.entity.Role;
import com.flowforge.security.entity.User;
import com.flowforge.security.jwt.JwtTokenProvider;
import com.flowforge.workflow.dto.CreateWorkflowRequest;
import com.flowforge.workflow.dto.WorkflowResponse;
import com.flowforge.workflow.service.WorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
public class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private WorkflowService workflowService;

    private String validToken;
    private UUID workflowId;
    private WorkflowResponse mockResponse;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        user.setRole(Role.ROLE_USER);
        validToken = jwtTokenProvider.generateAccessToken(user);

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
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(workflowId.toString()))
                .andExpect(jsonPath("$.data.name").value("Test Workflow"));
    }

    @Test
    void testGetWorkflow_Success() throws Exception {
        when(workflowService.getWorkflow(workflowId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/workflows/" + workflowId)
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Workflow"));
    }

    @Test
    void testListWorkflows_Success() throws Exception {
        when(workflowService.listWorkflows()).thenReturn(Collections.singletonList(mockResponse));

        mockMvc.perform(get("/api/v1/workflows")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(workflowId.toString()));
    }

    @Test
    void testAccessWithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/workflows"))
                .andExpect(status().isUnauthorized());
    }
}
