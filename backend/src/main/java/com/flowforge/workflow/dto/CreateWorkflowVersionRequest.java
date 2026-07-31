package com.flowforge.workflow.dto;

import com.flowforge.workflow.model.DagDefinition;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWorkflowVersionRequest {

    @NotNull(message = "DAG definition is required")
    private DagDefinition definition;
}
