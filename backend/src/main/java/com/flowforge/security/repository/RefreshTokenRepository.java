package com.flowforge.security.repository;

import com.flowforge.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.tokenFamilyId = :tokenFamilyId")
    void revokeFamily(@Param("tokenFamilyId") UUID tokenFamilyId, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.deviceId = :deviceId AND r.user.id = :userId")
    void revokeByDeviceIdAndUserId(@Param("deviceId") String deviceId, @Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :revokedAt WHERE r.user.id = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt <= :now OR r.revoked = true")
    int deleteExpiredOrRevokedTokens(@Param("now") Instant now);
}
