package com.flowforge.security.service;

import com.flowforge.security.config.JwtProperties;
import com.flowforge.security.entity.RefreshToken;
import com.flowforge.security.entity.User;
import com.flowforge.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateRefreshToken(User user, UUID tokenFamilyId, String deviceId, String ipAddress, String userAgent) {
        // Generate random 256-bit token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String tokenString = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(tokenString));
        refreshToken.setTokenFamilyId(tokenFamilyId == null ? UUID.randomUUID() : tokenFamilyId);
        refreshToken.setDeviceId(deviceId);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()));
        
        refreshTokenRepository.save(refreshToken);
        
        return tokenString;
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
