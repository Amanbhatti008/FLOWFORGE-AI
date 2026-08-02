package com.flowforge.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // RedissonSpringCacheManager is automatically configured by redisson-spring-boot-starter
    // We just need to enable caching here.
}
