package com.flowforge.workflow.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.flowforge.api.response.StandardResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate-workflow")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<StandardResponse<JsonNode>> generateWorkflow(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpServletRequest) {
        
        String traceId = (String) httpServletRequest.getAttribute("traceId");
        String prompt = request.getOrDefault("prompt", "Create a generic workflow");
        
        JsonNode workflowDag = aiService.generateWorkflow(prompt);
        
        return ResponseEntity.ok(StandardResponse.success(workflowDag, "Workflow generated successfully", traceId));
    }
}
