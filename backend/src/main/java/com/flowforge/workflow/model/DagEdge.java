package com.flowforge.workflow.model;

import lombok.Data;

@Data
public class DagEdge {
    private String source;
    private String target;
    private String condition; // Optional, for branch/conditional paths
}
