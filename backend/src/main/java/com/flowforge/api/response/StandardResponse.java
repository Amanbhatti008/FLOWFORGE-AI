package com.flowforge.api.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class StandardResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String traceId;
    @Builder.Default
    private String timestamp = Instant.now().toString();

    public static <T> StandardResponse<T> success(T data, String message, String traceId) {
        return StandardResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .traceId(traceId)
                .build();
    }
}
