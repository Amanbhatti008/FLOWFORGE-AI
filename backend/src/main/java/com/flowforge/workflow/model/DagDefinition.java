package com.flowforge.workflow.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DagDefinition {
    private List<DagNode> nodes;
    private List<DagEdge> edges;
    private Map<String, Object> globalConfig;
}
