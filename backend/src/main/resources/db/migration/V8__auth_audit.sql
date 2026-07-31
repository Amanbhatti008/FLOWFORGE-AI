-- Table for Refresh Tokens with Device and Token Family metadata
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE, -- SHA-256 hash of the random token
    token_family_id UUID NOT NULL,
    device_id VARCHAR(255),
    device_name VARCHAR(255),
    device_fingerprint_hash VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    last_used_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_family ON refresh_tokens(token_family_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);

-- Table for Active Sessions (Device Management)
CREATE TABLE active_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    device VARCHAR(255),
    ip VARCHAR(45),
    last_activity TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_active_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table for Immutable Audit Logs (Hash Chaining)
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    request_id VARCHAR(255),
    correlation_id VARCHAR(255),
    user_id UUID,
    endpoint VARCHAR(255),
    ip VARCHAR(45),
    user_agent TEXT,
    http_method VARCHAR(10),
    response_status INTEGER,
    duration_ms BIGINT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    previous_hash VARCHAR(64), -- SHA-256 hash for chain immutability
    current_hash VARCHAR(64) NOT NULL
);

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);

-- Table for Security Events
CREATE TABLE security_events (
    id UUID PRIMARY KEY,
    user_id UUID,
    event_type VARCHAR(100) NOT NULL, -- e.g., LOGIN_FAILED, TOKEN_REUSE, SUSPICIOUS_LOGIN
    ip_address VARCHAR(45),
    user_agent TEXT,
    details JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_security_events_type ON security_events(event_type, created_at);
CREATE INDEX idx_security_events_user ON security_events(user_id);

-- Skeleton Table for Password Reset Tokens
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
