package com.flowforge.workflow.dag;

import lombok.Getter;
import java.util.*;

@Getter
public class WorkflowGraph {
    private final Set<String> allNodes = new HashSet<>();
    private final Map<String, List<String>> adjacencyList = new HashMap<>();
    private final Map<String, Integer> inDegree = new HashMap<>();

    public void addNode(String nodeId) {
        allNodes.add(nodeId);
        adjacencyList.putIfAbsent(nodeId, new ArrayList<>());
        inDegree.putIfAbsent(nodeId, 0);
    }

    public void addEdge(String source, String target) {
        adjacencyList.get(source).add(target);
        inDegree.put(target, inDegree.getOrDefault(target, 0) + 1);
    }
}
