package com.flowforge.security.jwt;

import com.flowforge.security.config.JwtProperties;
import com.flowforge.security.entity.User;
import io.jsonwebtoken.*;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private KeyPair keyPair;
    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        // In a real production environment, load the RSA KeyPair from Kubernetes Secrets / Vault
        // via the paths specified in jwtProperties.privateKeyPath and publicKeyPath.
        // For demonstration, generating an in-memory RS256 KeyPair.
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            this.keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }

        // Configure Parser with 30s Clock Skew tolerance
        this.jwtParser = Jwts.parser()
                .verifyWith(this.keyPair.getPublic())
                .requireIssuer(jwtProperties.getIssuer())
                .requireAudience(jwtProperties.getAudience())
                .clockSkewSeconds(jwtProperties.getClockSkewSeconds())
                .build();
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .header()
                .keyId(jwtProperties.getKeyId())
                .and()
                .id(UUID.randomUUID().toString()) // jti
                .subject(user.getEmail()) // sub
                .issuer(jwtProperties.getIssuer())
                .audience().add(jwtProperties.getAudience()).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .claim("userId", user.getId().toString())
                .claim("roles", user.getRole().name())
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256) // Asymmetric RS256
                .compact();
    }

    public Claims validateTokenAndGetClaims(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.");
        }
        return null;
    }
}
