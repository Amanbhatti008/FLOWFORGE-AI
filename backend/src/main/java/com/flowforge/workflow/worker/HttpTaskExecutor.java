package com.flowforge.workflow.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowforge.workflow.domain.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class HttpTaskExecutor implements TaskExecutor {

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    @Override
    public boolean execute(Task task) throws Exception {
        log.info("Executing HTTP Task: {}", task.getId());
        
        // Simulate execution time for demo purposes (2 seconds)
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Parse inputData from String to JsonNode
        JsonNode inputData = null;
        if (task.getInputData() != null && !task.getInputData().isBlank()) {
            inputData = objectMapper.readTree(task.getInputData());
        }

        if (inputData == null || !inputData.has("url")) {
            // For the Viral Demo, we want tasks to succeed even without a URL
            log.info("No URL provided, simulating successful HTTP response for demo.");
            task.setOutputData("{\"status\":\"success\", \"demo\": true}");
            return true;
        }
        
        String url = inputData.get("url").asText();
        String methodStr = inputData.has("method") ? inputData.get("method").asText().toUpperCase() : "GET";
        HttpMethod method = HttpMethod.valueOf(methodStr);

        log.info("Making {} request to {}", method, url);
        
        RestClient.RequestBodySpec request = restClient.method(method)
                .uri(url);
                
        if (inputData.has("headers")) {
            JsonNode headersNode = inputData.get("headers");
            headersNode.fieldNames().forEachRemaining(key -> {
                request.header(key, headersNode.get(key).asText());
            });
        }
        
        if (inputData.has("body")) {
            request.body(inputData.get("body").toString());
            request.header("Content-Type", "application/json");
        }

        ResponseEntity<String> response = request.retrieve().toEntity(String.class);

        log.info("HTTP Task {} received status {}", task.getId(), response.getStatusCode());

        ObjectNode output = objectMapper.createObjectNode();
        output.put("statusCode", response.getStatusCode().value());
        if (response.getBody() != null) {
            output.put("body", response.getBody());
        }
        task.setOutputData(objectMapper.writeValueAsString(output));

        return response.getStatusCode().is2xxSuccessful();
    }

    @Override
    public String getType() {
        return "HTTP";
    }
}
