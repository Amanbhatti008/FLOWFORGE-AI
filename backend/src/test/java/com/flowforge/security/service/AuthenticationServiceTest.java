package com.flowforge.security.service;

import com.flowforge.api.exception.AuthenticationException;
import com.flowforge.security.dto.TokenPair;
import com.flowforge.security.entity.Role;
import com.flowforge.security.entity.User;
import com.flowforge.security.event.CustomAuthenticationEventPublisher;
import com.flowforge.security.jwt.JwtTokenProvider;
import com.flowforge.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @Mock
    private CustomAuthenticationEventPublisher eventPublisher;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@test.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.ROLE_USER);
    }

    @Test
    void login_Success() {
        when(bruteForceProtectionService.isLocked("test@test.com")).thenReturn(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(tokenProvider.generateAccessToken(testUser)).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(eq(testUser), isNull(), anyString(), anyString(), anyString())).thenReturn("refresh-token");

        TokenPair result = authenticationService.login("test@test.com", "password123", "deviceId", "127.0.0.1", "userAgent");

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        verify(bruteForceProtectionService).loginSucceeded("test@test.com");
        verify(eventPublisher).publishLoginSuccess("test@test.com", "127.0.0.1", "userAgent");
    }

    @Test
    void login_AccountLocked() {
        when(bruteForceProtectionService.isLocked("test@test.com")).thenReturn(true);

        assertThrows(AuthenticationException.class, () -> {
            authenticationService.login("test@test.com", "password123", "deviceId", "127.0.0.1", "userAgent");
        });

        verify(eventPublisher).publishAccountLocked("test@test.com", "127.0.0.1");
    }

    @Test
    void login_InvalidCredentials() {
        when(bruteForceProtectionService.isLocked("test@test.com")).thenReturn(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong-password", "encodedPassword")).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> {
            authenticationService.login("test@test.com", "wrong-password", "deviceId", "127.0.0.1", "userAgent");
        });

        verify(bruteForceProtectionService).loginFailed("test@test.com");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        authenticationService.register("new@test.com", "password123");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyInUse() {
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(AuthenticationException.class, () -> {
            authenticationService.register("test@test.com", "password123");
        });
    }
}
