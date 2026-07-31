package com.flowforge.workflow.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class AiService {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    private static final String WORKFLOW_SYSTEM_PROMPT = """
        You are a workflow orchestration engine assistant. Given a user's natural language description,
        generate a valid DAG (Directed Acyclic Graph) workflow definition as JSON.
        
        RULES:
        1. Return ONLY valid JSON, no markdown, no explanations.
        2. Each node must have: id (string like "1","2"...), type ("HTTP" or "SCRIPT"), name (human-readable),
           app (one of: "Mail", "Bot", "Database", "MessageSquare", "GitBranch", "Server", "FileCheck", "Upload", "Bell", "Globe"),
           position (object with x,y coordinates for visual layout).
        3. Edges connect nodes: each edge has "source" and "target" (node ids).
        4. The graph MUST be a DAG (no cycles).
        5. Position nodes vertically: first tier at y=50, second at y=200, third at y=350, etc.
           For parallel nodes, spread horizontally: x=100, x=300, x=500.
        6. Generate 4-7 nodes for a good workflow.
        
        OUTPUT FORMAT:
        {
          "nodes": [{"id":"1","type":"HTTP","name":"Step Name","app":"Mail","position":{"x":300,"y":50}}],
          "edges": [{"source":"1","target":"2"}]
        }
        """;

    private static final String RCA_SYSTEM_PROMPT = """
        You are a DevOps AI assistant specializing in root cause analysis for workflow task failures.
        Given a task execution context (task type, name, error message, retry count),
        provide a concise 1-2 sentence diagnosis and fix recommendation.
        
        Format: "AI Fix: [your diagnosis and recommendation]"
        
        Be specific and actionable. Examples:
        - "AI Fix: The HTTP endpoint returned 504 Gateway Timeout. Increase the HTTP client timeout from 30s to 60s or add a circuit breaker."
        - "AI Fix: Script execution failed due to undefined variable 'orderTotal'. Check the input data mapping from the previous task."
        """;

    /**
     * Generate a workflow DAG from a natural language prompt.
     */
    public JsonNode generateWorkflow(String userPrompt) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            log.warn("OpenAI API key not configured, returning demo workflow");
            return generateDemoWorkflow(userPrompt);
        }

        try {
            WebClient webClient = webClientBuilder.baseUrl(aiProperties.getBaseUrl()).build();

            Map<String, Object> requestBody = Map.of(
                    "model", aiProperties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", WORKFLOW_SYSTEM_PROMPT),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.7,
                    "response_format", Map.of("type", "json_object")
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            String content = responseJson.at("/choices/0/message/content").asText();
            return objectMapper.readTree(content);

        } catch (Exception e) {
            log.error("Failed to generate workflow via AI: {}", e.getMessage());
            return generateDemoWorkflow(userPrompt);
        }
    }

    /**
     * Analyze a task failure and return AI diagnosis.
     */
    public String analyzeFailure(String taskName, String taskType, String errorMessage, int retryCount) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            log.warn("OpenAI API key not configured, returning generic diagnosis");
            return "AI Fix: Check the " + taskType + " task configuration. The task '" + taskName +
                    "' failed after " + retryCount + " retries. Review the input parameters and external service availability.";
        }

        try {
            WebClient webClient = webClientBuilder.baseUrl(aiProperties.getBaseUrl()).build();

            String userMessage = String.format(
                    "Task Name: %s\nTask Type: %s\nError: %s\nRetry Count: %d",
                    taskName, taskType, errorMessage, retryCount
            );

            Map<String, Object> requestBody = Map.of(
                    "model", aiProperties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", RCA_SYSTEM_PROMPT),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.3,
                    "max_tokens", 150
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                    .block();

            JsonNode responseJson = objectMapper.readTree(response);
            return responseJson.at("/choices/0/message/content").asText();

        } catch (Exception e) {
            log.error("Failed to get AI RCA: {}", e.getMessage());
            return "AI Fix: Unable to analyze - check the " + taskType + " task configuration and external dependencies.";
        }
    }

    /**
     * Generates a smart demo workflow when no API key is configured.
     * Parses keywords from the user prompt to build a contextual workflow.
     */
    private JsonNode generateDemoWorkflow(String userPrompt) {
        String prompt = userPrompt.toLowerCase();
        try {
            String json;
            if (prompt.contains("order") || prompt.contains("payment") || prompt.contains("e-commerce")) {
                json = """
                    {
                      "nodes": [
                        {"id":"1","type":"HTTP","name":"Receive Order","app":"Globe","position":{"x":300,"y":50}},
                        {"id":"2","type":"SCRIPT","name":"Validate Order","app":"FileCheck","position":{"x":300,"y":200}},
                        {"id":"3","type":"HTTP","name":"Process Payment","app":"Database","position":{"x":100,"y":350}},
                        {"id":"4","type":"HTTP","name":"Reserve Inventory","app":"Server","position":{"x":500,"y":350}},
                        {"id":"5","type":"HTTP","name":"Send Confirmation Email","app":"Mail","position":{"x":300,"y":500}},
                        {"id":"6","type":"HTTP","name":"Notify Fulfillment","app":"Bell","position":{"x":300,"y":650}}
                      ],
                      "edges": [
                        {"source":"1","target":"2"},
                        {"source":"2","target":"3"},
                        {"source":"2","target":"4"},
                        {"source":"3","target":"5"},
                        {"source":"4","target":"5"},
                        {"source":"5","target":"6"}
                      ]
                    }""";
            } else if (prompt.contains("deploy") || prompt.contains("ci") || prompt.contains("build")) {
                json = """
                    {
                      "nodes": [
                        {"id":"1","type":"HTTP","name":"Trigger Build","app":"GitBranch","position":{"x":300,"y":50}},
                        {"id":"2","type":"SCRIPT","name":"Run Tests","app":"FileCheck","position":{"x":300,"y":200}},
                        {"id":"3","type":"SCRIPT","name":"Security Scan","app":"Bot","position":{"x":100,"y":350}},
                        {"id":"4","type":"HTTP","name":"Build Docker Image","app":"Server","position":{"x":500,"y":350}},
                        {"id":"5","type":"HTTP","name":"Deploy to K8s","app":"Upload","position":{"x":300,"y":500}},
                        {"id":"6","type":"HTTP","name":"Notify Slack","app":"MessageSquare","position":{"x":300,"y":650}}
                      ],
                      "edges": [
                        {"source":"1","target":"2"},
                        {"source":"2","target":"3"},
                        {"source":"2","target":"4"},
                        {"source":"3","target":"5"},
                        {"source":"4","target":"5"},
                        {"source":"5","target":"6"}
                      ]
                    }""";
            } else if (prompt.contains("data") || prompt.contains("etl") || prompt.contains("pipeline")) {
                json = """
                    {
                      "nodes": [
                        {"id":"1","type":"HTTP","name":"Extract Data","app":"Database","position":{"x":300,"y":50}},
                        {"id":"2","type":"SCRIPT","name":"Clean & Transform","app":"Bot","position":{"x":300,"y":200}},
                        {"id":"3","type":"SCRIPT","name":"Validate Schema","app":"FileCheck","position":{"x":300,"y":350}},
                        {"id":"4","type":"HTTP","name":"Load to Warehouse","app":"Database","position":{"x":300,"y":500}},
                        {"id":"5","type":"HTTP","name":"Generate Report","app":"Mail","position":{"x":300,"y":650}}
                      ],
                      "edges": [
                        {"source":"1","target":"2"},
                        {"source":"2","target":"3"},
                        {"source":"3","target":"4"},
                        {"source":"4","target":"5"}
                      ]
                    }""";
            } else {
                json = """
                    {
                      "nodes": [
                        {"id":"1","type":"HTTP","name":"Start Process","app":"Globe","position":{"x":300,"y":50}},
                        {"id":"2","type":"SCRIPT","name":"AI Analysis","app":"Bot","position":{"x":300,"y":200}},
                        {"id":"3","type":"HTTP","name":"Store Results","app":"Database","position":{"x":100,"y":350}},
                        {"id":"4","type":"HTTP","name":"Send Notification","app":"Bell","position":{"x":500,"y":350}},
                        {"id":"5","type":"HTTP","name":"Send Email Report","app":"Mail","position":{"x":300,"y":500}}
                      ],
                      "edges": [
                        {"source":"1","target":"2"},
                        {"source":"2","target":"3"},
                        {"source":"2","target":"4"},
                        {"source":"3","target":"5"},
                        {"source":"4","target":"5"}
                      ]
                    }""";
            }
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate demo workflow", e);
        }
    }
}
