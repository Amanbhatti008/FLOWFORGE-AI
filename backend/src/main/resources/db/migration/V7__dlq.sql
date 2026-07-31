-- Table for Dead Letter Queue (DLQ) messages
CREATE TABLE dlq_messages (
    id UUID PRIMARY KEY,
    message_payload JSONB NOT NULL, -- The original Kafka message that failed
    error_reason TEXT NOT NULL,     -- The exception or reason for failure
    failed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    retry_count INTEGER DEFAULT 0 NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'FAILED', -- FAILED, REPLAYED, IGNORED
    replayed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_dlq_status CHECK (status IN ('FAILED', 'REPLAYED', 'IGNORED'))
);

-- Index for the Admin Dashboard DLQ Manager UI
CREATE INDEX idx_dlq_messages_status ON dlq_messages(status, failed_at);
