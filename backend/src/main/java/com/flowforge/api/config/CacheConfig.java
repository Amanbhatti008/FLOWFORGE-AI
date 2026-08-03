package com.flowforge.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public org.springframework.cache.CacheManager cacheManager(org.redisson.api.RedissonClient redissonClient) {
        // Explicitly defining the cache manager with dynamic cache creation
        org.redisson.spring.cache.RedissonSpringCacheManager cacheManager = 
            new org.redisson.spring.cache.RedissonSpringCacheManager(redissonClient);
        
        // This explicitly tells the cache manager to create caches dynamically
        // Redisson handles dynamic cache creation by default, but defining it explicitly
        // prevents Spring Boot's generic ConcurrentMapCacheManager from taking over
        // and throwing 'Cannot find cache named X'
        return cacheManager;
    }
}
