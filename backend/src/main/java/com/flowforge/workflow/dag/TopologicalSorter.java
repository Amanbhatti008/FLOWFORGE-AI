package com.flowforge.workflow.dag;

import java.util.*;

public class TopologicalSorter {

    /**
     * Sorts the graph topologically and returns groups of nodes that can be executed in parallel.
     * Each sub-list in the returned list represents a parallel execution tier.
     */
    public static List<List<String>> sort(WorkflowGraph graph) {
        Map<String, Integer> inDegree = new HashMap<>(graph.getInDegree());
        Queue<String> queue = new LinkedList<>();

        // Enqueue nodes with 0 in-degree (Initial tasks)
        for (String node : graph.getAllNodes()) {
            if (inDegree.getOrDefault(node, 0) == 0) {
                queue.offer(node);
            }
        }

        List<List<String>> executionOrder = new ArrayList<>();
        int processedNodes = 0;

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<String> currentLevel = new ArrayList<>();

            for (int i = 0; i < levelSize; i++) {
                String node = queue.poll();
                currentLevel.add(node);
                processedNodes++;

                List<String> neighbors = graph.getAdjacencyList().get(node);
                if (neighbors != null) {
                    for (String neighbor : neighbors) {
                        int currentInDegree = inDegree.get(neighbor) - 1;
                        inDegree.put(neighbor, currentInDegree);
                        if (currentInDegree == 0) {
                            queue.offer(neighbor);
                        }
                    }
                }
            }
            executionOrder.add(currentLevel);
        }

        if (processedNodes != graph.getAllNodes().size()) {
            throw new IllegalStateException("Graph contains a cycle and cannot be topologically sorted.");
        }

        return executionOrder;
    }
}
