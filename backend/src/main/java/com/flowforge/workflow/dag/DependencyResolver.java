package com.flowforge.workflow.dag;

import com.flowforge.workflow.model.DagDefinition;
import com.flowforge.workflow.model.DagEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("null")
public class DependencyResolver {

    public static List<String> getInitialNodes(DagDefinition dag) {
        Set<String> targetNodes = (dag.getEdges() != null ? dag.getEdges() : new ArrayList<DagEdge>()).stream()
                .map(DagEdge::getTarget)
                .collect(Collectors.toSet());

        return (dag.getNodes() != null ? dag.getNodes() : new ArrayList<com.flowforge.workflow.model.DagNode>()).stream()
                .filter(node -> !targetNodes.contains(node.getId()))
                .map(node -> node.getId())
                .collect(Collectors.toList());
    }

    public static List<String> getUnlockedNodes(DagDefinition dag, Set<String> completedNodes, Set<String> skippedNodes, Set<String> alreadyScheduledOrRunning) {
        List<String> unlocked = new ArrayList<>();
        WorkflowGraph graph = DagParser.parse(dag);
        
        for (String node : graph.getAllNodes()) {
            if (completedNodes.contains(node) || skippedNodes.contains(node) || alreadyScheduledOrRunning.contains(node)) {
                continue;
            }

            boolean allDependenciesMet = true;
            if (dag.getEdges() != null) {
                for (DagEdge edge : dag.getEdges()) {
                    if (edge.getTarget().equals(node)) {
                        // A dependency is met if it's either COMPLETED or SKIPPED
                        if (!completedNodes.contains(edge.getSource()) && !skippedNodes.contains(edge.getSource())) {
                            allDependenciesMet = false;
                            break;
                        }
                    }
                }
            }

            if (allDependenciesMet) {
                unlocked.add(node);
            }
        }
        return unlocked;
    }
}
