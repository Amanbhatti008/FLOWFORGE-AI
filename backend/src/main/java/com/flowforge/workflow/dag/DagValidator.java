package com.flowforge.workflow.dag;

import com.flowforge.workflow.model.DagDefinition;

public class DagValidator {

    public static void validate(DagDefinition definition) {
        if (definition == null || definition.getNodes() == null || definition.getNodes().isEmpty()) {
            throw new IllegalArgumentException("Workflow definition must contain at least one node.");
        }

        // 1. Parse into mathematical graph
        WorkflowGraph graph = DagParser.parse(definition);

        // 2. Check for cycles (Throws Exception if cycle exists)
        CycleDetector.detectCycleOrThrow(graph);
        
        // 3. (Optional) Run topological sort just to verify full structure correctness
        TopologicalSorter.sort(graph);
    }
}
