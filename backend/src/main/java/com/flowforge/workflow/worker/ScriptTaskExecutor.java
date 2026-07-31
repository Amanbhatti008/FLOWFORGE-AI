package com.flowforge.workflow.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flowforge.workflow.domain.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScriptTaskExecutor implements TaskExecutor {

    private final ObjectMapper objectMapper;

    @Override
    public boolean execute(Task task) throws Exception {
        log.info("Executing Script Task: {}", task.getId());
        
        // Simulate execution time for demo purposes (3 seconds for script)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        JsonNode inputData = null;
        if (task.getInputData() != null && !task.getInputData().isBlank()) {
            inputData = objectMapper.readTree(task.getInputData());
        }
        
        if (inputData == null || !inputData.has("script")) {
            log.info("No script provided, simulating successful script execution for demo.");
            task.setOutputData("{\"result\":\"demo_success\", \"sentiment\":\"positive\", \"confidence\":0.92}");
            return true;
        }
        
        String script = inputData.get("script").asText();
        log.debug("Evaluating Script: {}", script);
        
        // Use GraalVM Polyglot for JS execution
        try (Context context = Context.newBuilder("js").build()) {
            
            // Inject variables from inputData into script context
            if (inputData.has("variables")) {
                JsonNode vars = inputData.get("variables");
                vars.fieldNames().forEachRemaining(key -> {
                    JsonNode val = vars.get(key);
                    if (val.isNumber()) context.getBindings("js").putMember(key, val.numberValue());
                    else if (val.isBoolean()) context.getBindings("js").putMember(key, val.booleanValue());
                    else context.getBindings("js").putMember(key, val.asText());
                });
            }
            
            // Execute
            Value result = context.eval("js", 
                "(function() { " + script + " })();"
            );
            
            ObjectNode output = objectMapper.createObjectNode();
            
            if (result.isNull()) {
                output.putNull("result");
            } else if (result.isNumber()) {
                output.put("result", result.asDouble());
            } else if (result.isBoolean()) {
                output.put("result", result.asBoolean());
            } else {
                output.put("result", result.asString());
            }
            
            task.setOutputData(objectMapper.writeValueAsString(output));
            return true;
        } catch (Exception e) {
            log.error("Script execution failed for task {}: {}", task.getId(), e.getMessage());
            ObjectNode output = objectMapper.createObjectNode();
            output.put("error", e.getMessage());
            task.setOutputData(objectMapper.writeValueAsString(output));
            return false;
        }
    }

    @Override
    public String getType() {
        return "SCRIPT";
    }
}
