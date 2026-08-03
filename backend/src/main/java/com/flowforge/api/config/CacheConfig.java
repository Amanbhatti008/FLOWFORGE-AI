package com.flowforge.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public org.springframework.cache.CacheManager cacheManager(org.redisson.api.RedissonClient redissonClient) {
        // Redisson in newer versions requires explicit cache configuration
        // to prevent returning null for undefined caches
        java.util.Map<String, org.redisson.spring.cache.CacheConfig> config = new java.util.HashMap<>();
        
        // Define workflows_list cache (TTL: 10 mins, MaxIdleTime: 5 mins)
        config.put("workflows_list", new org.redisson.spring.cache.CacheConfig(10 * 60 * 1000, 5 * 60 * 1000));
        
        // Define workflows cache (TTL: 10 mins, MaxIdleTime: 5 mins)
        config.put("workflows", new org.redisson.spring.cache.CacheConfig(10 * 60 * 1000, 5 * 60 * 1000));
        
        return new org.redisson.spring.cache.RedissonSpringCacheManager(redissonClient, config);
    }
}
