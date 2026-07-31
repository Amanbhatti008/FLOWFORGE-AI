package com.flowforge.workflow.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "flowforge.ai")
public class AiProperties {
    private String apiKey = "";
    private String model = "gpt-4o";
    private String baseUrl = "https://api.openai.com/v1";
    private int timeoutSeconds = 30;
}
