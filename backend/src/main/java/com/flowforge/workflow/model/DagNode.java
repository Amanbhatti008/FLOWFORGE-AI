package com.flowforge.workflow.model;

import lombok.Data;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DagNode {
    private String id; // Matches task_ref_name
    private String type; // e.g., HTTP, SCRIPT, DB
    private String name;
    private Map<String, Object> inputParameters;
    private int retryCount = 0;
    
    // UI Metadata
    private String app;
    private Map<String, Double> position;
}
