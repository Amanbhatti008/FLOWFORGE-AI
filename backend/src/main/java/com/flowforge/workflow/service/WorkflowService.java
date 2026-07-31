package com.flowforge.workflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.api.exception.AuthenticationException;
import com.flowforge.security.entity.User;
import com.flowforge.security.repository.UserRepository;
import com.flowforge.workflow.dto.CreateWorkflowRequest;
import com.flowforge.workflow.dto.CreateWorkflowVersionRequest;
import com.flowforge.workflow.dto.WorkflowResponse;
import com.flowforge.workflow.domain.Workflow;
import com.flowforge.workflow.domain.WorkflowVersion;
import com.flowforge.workflow.repository.WorkflowRepository;
import com.flowforge.workflow.repository.WorkflowVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        Workflow workflow = new Workflow();
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setCreatedBy(user);

        workflow = workflowRepository.save(workflow);

        return mapToResponse(workflow, 0);
    }

    @Transactional
    public WorkflowResponse createWorkflowVersion(UUID workflowId, CreateWorkflowVersionRequest request) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

        Integer latestVersion = workflowVersionRepository.findLatestByWorkflowId(workflowId)
                .map(WorkflowVersion::getVersionNumber)
                .orElse(0);

        Integer newVersionNumber = latestVersion + 1;

        // Validates DAG structure mathematically (Cycles, etc)
        com.flowforge.workflow.dag.DagValidator.validate(request.getDefinition());

        WorkflowVersion newVersion = new WorkflowVersion();
        newVersion.setWorkflow(workflow);
        newVersion.setVersionNumber(newVersionNumber);

        try {
            String jsonDefinition = objectMapper.writeValueAsString(request.getDefinition());
            newVersion.setDefinitionJson(jsonDefinition);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid DAG definition format");
        }

        workflowVersionRepository.save(newVersion);
        
        // Update workflow updated_at
        workflowRepository.save(workflow);

        return mapToResponse(workflow, newVersionNumber);
    }
    
    @Transactional(readOnly = true)
    public List<WorkflowResponse> listWorkflows() {
        return workflowRepository.findAll().stream()
                .map(w -> mapToResponse(w, getLatestVersionNumber(w.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflow(UUID id) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));
        return mapToResponse(workflow, getLatestVersionNumber(id));
    }

    @Transactional
    public WorkflowResponse updateWorkflow(UUID id, CreateWorkflowRequest request) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));
        
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow = workflowRepository.save(workflow);
        
        return mapToResponse(workflow, getLatestVersionNumber(id));
    }

    @Transactional
    public void deleteWorkflow(UUID id) {
        if (!workflowRepository.existsById(id)) {
            throw new IllegalArgumentException("Workflow not found");
        }
        workflowRepository.deleteById(id);
    }

    private Integer getLatestVersionNumber(UUID workflowId) {
        return workflowVersionRepository.findLatestByWorkflowId(workflowId)
                .map(WorkflowVersion::getVersionNumber)
                .orElse(0);
    }

    private WorkflowResponse mapToResponse(Workflow workflow, Integer latestVersion) {
        return WorkflowResponse.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .latestVersion(latestVersion)
                .build();
    }
}
