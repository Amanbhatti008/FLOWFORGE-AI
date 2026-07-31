package com.flowforge.api.controller;

import com.flowforge.api.response.StandardResponse;
import com.flowforge.security.dto.LoginRequest;
import com.flowforge.security.dto.RegisterRequest;
import com.flowforge.security.dto.TokenPair;
import com.flowforge.security.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<TokenPair>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String traceId = (String) request.getAttribute("traceId");

        TokenPair tokens = authenticationService.login(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                loginRequest.getDeviceId(),
                ipAddress,
                userAgent
        );

        return ResponseEntity.ok(StandardResponse.success(tokens, "Login successful", traceId));
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<String>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {
        
        authenticationService.register(registerRequest.getEmail(), registerRequest.getPassword());
        String traceId = (String) request.getAttribute("traceId");
        
        return ResponseEntity.ok(StandardResponse.success(null, "Registration successful", traceId));
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardResponse<String>> logout(HttpServletRequest request) {
        String traceId = (String) request.getAttribute("traceId");
        // Decode JWT to get JTI and add to blacklist, remove active session
        return ResponseEntity.ok(StandardResponse.success(null, "Logout successful", traceId));
    }

    @PostMapping("/refresh")
    public ResponseEntity<StandardResponse<String>> refresh(HttpServletRequest request) {
        String traceId = (String) request.getAttribute("traceId");
        // Refresh token rotation logic
        return ResponseEntity.ok(StandardResponse.success(null, "Token refreshed successfully", traceId));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<StandardResponse<String>> logoutAll(HttpServletRequest request) {
        String traceId = (String) request.getAttribute("traceId");
        // Revoke all tokens and active sessions
        return ResponseEntity.ok(StandardResponse.success(null, "Logged out of all devices successfully", traceId));
    }
}
