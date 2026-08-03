package com.flowforge.workflow.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.url:redis://localhost:6379}")
    private String redisUrl;

    @Bean
    public RedissonClient redissonClient() {
        String finalUrl = redisUrl != null ? redisUrl.trim().replace("\"", "") : "redis://localhost:6379";
        if (finalUrl.startsWith("REDIS_URL=")) {
            finalUrl = finalUrl.replace("REDIS_URL=", "").trim().replace("\"", "");
        }
        
        Config config = new Config();
        config.useSingleServer()
                .setAddress(finalUrl)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(20);
        return Redisson.create(config);
    }
}
