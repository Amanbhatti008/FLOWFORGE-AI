package com.flowforge.security.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BruteForceProtectionService {

    private final RedissonClient redissonClient;
    private final int MAX_ATTEMPTS = 5;
    private final long LOCK_TIME_MINUTES = 15;

    public void loginFailed(String key) {
        RMapCache<String, Integer> attemptsCache = redissonClient.getMapCache("loginAttempts");
        Integer attempts = attemptsCache.get(key);
        
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;
        
        // Cache entry lives for slightly longer than the lock time
        attemptsCache.put(key, attempts, LOCK_TIME_MINUTES + 1, TimeUnit.MINUTES);
    }

    public boolean isLocked(String key) {
        RMapCache<String, Integer> attemptsCache = redissonClient.getMapCache("loginAttempts");
        Integer attempts = attemptsCache.get(key);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    public void loginSucceeded(String key) {
        RMapCache<String, Integer> attemptsCache = redissonClient.getMapCache("loginAttempts");
        attemptsCache.remove(key);
    }
}
