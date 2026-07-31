package com.flowforge.workflow.dto;

import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class TriggerWorkflowRequest {
    
    // Optional, if empty the latest version will be used
    private UUID workflowVersionId;

    // Optional input data for the workflow execution
    private Map<String, Object> inputData;
}
