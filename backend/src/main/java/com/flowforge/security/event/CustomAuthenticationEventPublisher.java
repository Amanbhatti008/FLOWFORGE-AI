package com.flowforge.security.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEventPublisher {



    public void publishLoginSuccess(String email, String ipAddress, String userAgent) {
        log.info("LOGIN_SUCCESS: {}", email);
        // Dispatch spring application event which will be captured and written to Outbox
    }

    public void publishLoginFailed(String email, String ipAddress, String userAgent, String reason) {
        log.warn("LOGIN_FAILED: {} reason: {}", email, reason);
        // Dispatch spring application event
    }

    public void publishAccountLocked(String email, String ipAddress) {
        log.warn("ACCOUNT_LOCKED: {}", email);
        // Dispatch spring application event
    }

    public void publishTokenRefreshed(String email, String deviceId) {
        log.info("TOKEN_REFRESHED: {} on device {}", email, deviceId);
        // Dispatch spring application event
    }

    public void publishTokenReuseDetected(String email, String deviceId) {
        log.warn("TOKEN_REUSE_DETECTED: {} on device {}", email, deviceId);
        // Dispatch spring application event
    }
}
