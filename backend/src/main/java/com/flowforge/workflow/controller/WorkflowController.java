package com.flowforge.workflow.controller;

import com.flowforge.api.response.StandardResponse;
import com.flowforge.workflow.dto.CreateWorkflowRequest;
import com.flowforge.workflow.dto.CreateWorkflowVersionRequest;
import com.flowforge.workflow.dto.TriggerWorkflowRequest;
import com.flowforge.workflow.dto.WorkflowExecutionResponse;
import com.flowforge.workflow.dto.WorkflowResponse;
import com.flowforge.workflow.service.WorkflowService;
import com.flowforge.workflow.service.WorkflowTriggerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowTriggerService workflowTriggerService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<WorkflowResponse>> createWorkflow(
            @Valid @RequestBody CreateWorkflowRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest) {
        
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        String email = authentication.getName();
        
        WorkflowResponse response = workflowService.createWorkflow(request, email);
        return ResponseEntity.ok(StandardResponse.success(response, "Workflow created successfully", traceId));
    }

    @PostMapping("/{id}/versions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<WorkflowResponse>> createWorkflowVersion(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWorkflowVersionRequest request,
            HttpServletRequest httpServletRequest) {
        
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        WorkflowResponse response = workflowService.createWorkflowVersion(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Workflow version created successfully", traceId));
    }

    @PostMapping("/execute")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<WorkflowExecutionResponse>> executeAdhoc(
            @Valid @RequestBody java.util.Map<String, Object> requestBody,
            Authentication authentication,
            HttpServletRequest httpServletRequest) {
        
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        String email = authentication.getName();
        
        System.out.println("====== RECEIVED REACT FLOW JSON ======");
        System.out.println(requestBody);
        System.out.println("======================================");
        
        // Under the hood, create a temporary workflow to run the execution engine
        CreateWorkflowRequest createReq = new CreateWorkflowRequest();
        createReq.setName("Adhoc Workflow " + System.currentTimeMillis());
        createReq.setDescription("Created from Adhoc Execute API");
        WorkflowResponse wf = workflowService.createWorkflow(createReq, email);
        
        CreateWorkflowVersionRequest versionReq = new CreateWorkflowVersionRequest();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        versionReq.setDefinition(mapper.convertValue(requestBody, com.flowforge.workflow.model.DagDefinition.class));
        workflowService.createWorkflowVersion(wf.getId(), versionReq);
        
        WorkflowExecutionResponse response = workflowTriggerService.triggerWorkflow(wf.getId(), new TriggerWorkflowRequest(), email);
        return ResponseEntity.ok(StandardResponse.success(response, "Adhoc workflow executed successfully", traceId));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<List<WorkflowResponse>>> listWorkflows(HttpServletRequest httpServletRequest) {
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        List<WorkflowResponse> response = workflowService.listWorkflows();
        return ResponseEntity.ok(StandardResponse.success(response, "Workflows retrieved successfully", traceId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<WorkflowResponse>> getWorkflow(
            @PathVariable UUID id,
            HttpServletRequest httpServletRequest) {
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        WorkflowResponse response = workflowService.getWorkflow(id);
        return ResponseEntity.ok(StandardResponse.success(response, "Workflow retrieved successfully", traceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<WorkflowResponse>> updateWorkflow(
            @PathVariable UUID id,
            @Valid @RequestBody CreateWorkflowRequest request,
            HttpServletRequest httpServletRequest) {
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        WorkflowResponse response = workflowService.updateWorkflow(id, request);
        return ResponseEntity.ok(StandardResponse.success(response, "Workflow updated successfully", traceId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponse<String>> deleteWorkflow(
            @PathVariable UUID id,
            HttpServletRequest httpServletRequest) {
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        workflowService.deleteWorkflow(id);
        return ResponseEntity.ok(StandardResponse.success(null, "Workflow deleted successfully", traceId));
    }

    @PostMapping("/{id}/execute")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<WorkflowExecutionResponse>> triggerWorkflow(
            @PathVariable UUID id,
            @RequestBody(required = false) TriggerWorkflowRequest request,
            Authentication authentication,
            HttpServletRequest httpServletRequest) {
        
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        String email = authentication.getName();
        
        if (request == null) {
            request = new TriggerWorkflowRequest(); // Default empty request
        }
        
        WorkflowExecutionResponse response = workflowTriggerService.triggerWorkflow(id, request, email);
        return ResponseEntity.ok(StandardResponse.success(response, "Workflow triggered successfully", traceId));
    }
}
