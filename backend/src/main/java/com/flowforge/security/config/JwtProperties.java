package com.flowforge.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String privateKeyPath;
    private String publicKeyPath;
    private long accessTokenExpirationMs = 900000; // 15 mins
    private long refreshTokenExpirationMs = 604800000; // 7 days
    private String issuer = "FlowForge";
    private String audience = "FlowForge-Clients";
    private long clockSkewSeconds = 30;
    private String keyId = "key-2026-01";
}
