package com.flowforge.workflow.dag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ConditionEvaluator {

    private final ObjectMapper objectMapper;

    public ConditionEvaluator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Evaluates a JavaScript condition expression against the provided JSON output data.
     * The output data is bound to a variable named "output".
     *
     * @param condition  JavaScript boolean expression (e.g., "output.statusCode == 200")
     * @param outputData JSON string representing the output of the source task
     * @return true if condition evaluates to true, false otherwise
     */
    public boolean evaluate(String condition, String outputData) {
        if (condition == null || condition.trim().isEmpty()) {
            return true; // No condition means always true
        }

        try (Context context = Context.newBuilder("js").build()) {
            if (outputData != null && !outputData.trim().isEmpty()) {
                Map<String, Object> outputMap = objectMapper.readValue(outputData, new TypeReference<>() {});
                // Bind the map directly to polyglot context
                context.getBindings("js").putMember("output", outputMap);
            } else {
                context.getBindings("js").putMember("output", Map.of());
            }

            Value result = context.eval("js", condition);
            return result.asBoolean();
        } catch (JsonProcessingException e) {
            log.error("Failed to parse outputData for condition evaluation: {}", outputData, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to evaluate condition: '{}'", condition, e);
            return false;
        }
    }
}
