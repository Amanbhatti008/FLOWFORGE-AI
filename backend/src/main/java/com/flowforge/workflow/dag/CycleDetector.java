package com.flowforge.workflow.dag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CycleDetector {

    private enum Color { WHITE, GRAY, BLACK }

    public static void detectCycleOrThrow(WorkflowGraph graph) {
        Map<String, Color> colors = new HashMap<>();
        for (String node : graph.getAllNodes()) {
            colors.put(node, Color.WHITE);
        }

        for (String node : graph.getAllNodes()) {
            if (colors.get(node) == Color.WHITE) {
                if (hasCycleDFS(node, graph, colors)) {
                    throw new IllegalArgumentException("Invalid DAG: A cycle was detected in the workflow graph.");
                }
            }
        }
    }

    private static boolean hasCycleDFS(String node, WorkflowGraph graph, Map<String, Color> colors) {
        colors.put(node, Color.GRAY);

        List<String> neighbors = graph.getAdjacencyList().get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                Color neighborColor = colors.get(neighbor);
                if (neighborColor == Color.GRAY) {
                    return true; // Cycle detected
                }
                if (neighborColor == Color.WHITE && hasCycleDFS(neighbor, graph, colors)) {
                    return true;
                }
            }
        }

        colors.put(node, Color.BLACK);
        return false;
    }
}
