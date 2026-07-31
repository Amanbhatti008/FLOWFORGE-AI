package com.flowforge.workflow.dag;

import com.flowforge.workflow.model.DagDefinition;
import com.flowforge.workflow.model.DagEdge;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("null")
public class DependencyResolver {

    /**
     * Finds the initial nodes (in-degree 0) for a brand-new workflow execution.
     */
    public static List<String> getInitialNodes(DagDefinition dag) {
        Set<String> targetNodes = dag.getEdges().stream()
                .map(DagEdge::getTarget)
                .collect(Collectors.toSet());

        return dag.getNodes().stream()
                .filter(node -> !targetNodes.contains(node.getId()))
                .map(node -> node.getId())
                .collect(Collectors.toList());
    }

    /**
     * Given a list of successfully completed node IDs in the current execution,
     * calculate which nodes are now unlocked and ready to be scheduled.
     * A node is unlocked if ALL of its incoming dependencies are in the completedNodes set.
     */
    public static List<String> getUnlockedNodes(DagDefinition dag, Set<String> completedNodes, Set<String> alreadyScheduledOrRunning) {
        List<String> unlocked = new ArrayList<>();

        WorkflowGraph graph = DagParser.parse(dag);
        
        for (String node : graph.getAllNodes()) {
            // Skip if already completed or currently running/scheduled
            if (completedNodes.contains(node) || alreadyScheduledOrRunning.contains(node)) {
                continue;
            }

            // Find all incoming edges for this node
            boolean allDependenciesMet = true;
            for (DagEdge edge : dag.getEdges()) {
                if (edge.getTarget().equals(node)) {
                    if (!completedNodes.contains(edge.getSource())) {
                        allDependenciesMet = false;
                        break;
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
