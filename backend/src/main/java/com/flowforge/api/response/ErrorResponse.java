package com.flowforge.api.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {
    private boolean success;
    private String errorCode;
    private String message;
    private Map<String, String> validationErrors;
    private String traceId;
    @Builder.Default
    private String timestamp = Instant.now().toString();
}
