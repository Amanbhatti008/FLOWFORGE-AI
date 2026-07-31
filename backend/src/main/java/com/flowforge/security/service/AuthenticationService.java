package com.flowforge.security.service;

import com.flowforge.api.exception.AuthenticationException;
import com.flowforge.security.dto.TokenPair;
import com.flowforge.security.entity.User;
import com.flowforge.security.event.CustomAuthenticationEventPublisher;
import com.flowforge.security.jwt.JwtTokenProvider;
import com.flowforge.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final CustomAuthenticationEventPublisher eventPublisher;

    @Transactional
    public TokenPair login(String email, String password, String deviceId, String ipAddress, String userAgent) {
        if (bruteForceProtectionService.isLocked(email)) {
            eventPublisher.publishAccountLocked(email, ipAddress);
            throw new AuthenticationException("Account is temporarily locked due to too many failed login attempts.");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            bruteForceProtectionService.loginFailed(email);
            eventPublisher.publishLoginFailed(email, ipAddress, userAgent, "Invalid credentials");
            throw new AuthenticationException("Invalid email or password");
        }

        bruteForceProtectionService.loginSucceeded(email);
        
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshTokenString = refreshTokenService.generateRefreshToken(user, null, deviceId, ipAddress, userAgent);

        eventPublisher.publishLoginSuccess(email, ipAddress, userAgent);
        
        return new TokenPair(accessToken, refreshTokenString);
    }

    @Transactional
    public void register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new AuthenticationException("Email is already in use");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(com.flowforge.security.entity.Role.ROLE_USER);

        userRepository.save(user);
    }
}
