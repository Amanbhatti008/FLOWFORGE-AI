package com.flowforge.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final JdbcTemplate jdbcTemplate;

    @Around("@within(org.springframework.web.bind.annotation.RestController) && (@annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.PutMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public Object logAuditActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        int status = 200;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 500;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String method = request.getMethod();
                    String endpoint = request.getRequestURI();
                    String ip = request.getRemoteAddr();
                    String userAgent = request.getHeader("User-Agent");
                    String traceId = (String) request.getAttribute("traceId");

                    String data = traceId + endpoint + status + duration;
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
                    StringBuilder hexString = new StringBuilder(2 * hash.length);
                    for (byte b : hash) {
                        String hex = Integer.toHexString(0xff & b);
                        if(hex.length() == 1) {
                            hexString.append('0');
                        }
                        hexString.append(hex);
                    }
                    String currentHash = hexString.toString();

                    jdbcTemplate.update("INSERT INTO audit_logs (id, request_id, endpoint, ip, user_agent, http_method, response_status, duration_ms, timestamp, current_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            UUID.randomUUID(), traceId, endpoint, ip, userAgent, method, status, duration, OffsetDateTime.now(), currentHash);
                }
            } catch (Exception ex) {
                log.error("Failed to save audit log", ex);
            }
        }
    }
}
