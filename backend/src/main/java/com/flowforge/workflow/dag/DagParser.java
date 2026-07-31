package com.flowforge.workflow.dag;

import com.flowforge.workflow.model.DagDefinition;
import com.flowforge.workflow.model.DagEdge;
import com.flowforge.workflow.model.DagNode;

public class DagParser {

    public static WorkflowGraph parse(DagDefinition definition) {
        WorkflowGraph graph = new WorkflowGraph();

        if (definition == null) {
            return graph;
        }

        // Add all nodes
        if (definition.getNodes() != null) {
            for (DagNode node : definition.getNodes()) {
                if (node.getId() == null || node.getId().trim().isEmpty()) {
                    throw new IllegalArgumentException("Node ID cannot be null or empty");
                }
                graph.addNode(node.getId());
            }
        }

        // Add all edges
        if (definition.getEdges() != null) {
            for (DagEdge edge : definition.getEdges()) {
                if (!graph.getAllNodes().contains(edge.getSource())) {
                    throw new IllegalArgumentException("Edge source node not found: " + edge.getSource());
                }
                if (!graph.getAllNodes().contains(edge.getTarget())) {
                    throw new IllegalArgumentException("Edge target node not found: " + edge.getTarget());
                }
                graph.addEdge(edge.getSource(), edge.getTarget());
            }
        }

        return graph;
    }
}
