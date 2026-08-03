package com.flowforge.workflow.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.url:}")
    private String springDataRedisUrl;

    @Bean
    public RedissonClient redissonClient() {
        String finalUrl = System.getenv("REDIS_URL");
        
        if (finalUrl == null || finalUrl.trim().isEmpty()) {
            finalUrl = springDataRedisUrl;
        }
        
        if (finalUrl == null || finalUrl.trim().isEmpty()) {
            finalUrl = "redis://localhost:6379";
        }
        
        // Find the actual start of the protocol to ignore any hidden characters, quotes, or zero-width spaces
        int idx = finalUrl.indexOf("rediss://");
        if (idx == -1) {
            idx = finalUrl.indexOf("redis://");
        }
        
        if (idx != -1) {
            finalUrl = finalUrl.substring(idx);
            // Also trim any trailing quotes or spaces
            finalUrl = finalUrl.replaceAll("[\"\\s]+$", "");
        } else {
            // Throw a very clear error message so we can see exactly what was passed
            throw new IllegalArgumentException("FATAL CONFIG ERROR: The REDIS_URL environment variable is invalid. Actual value received: [" + finalUrl + "]. Please ensure it contains redis:// or rediss://");
        }
        
        Config config = new Config();
        config.useSingleServer()
                .setAddress(finalUrl)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20);
        return Redisson.create(config);
    }
}
