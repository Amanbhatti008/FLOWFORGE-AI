-- Table for Transaction Outbox Pattern
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL, -- e.g., workflow_execution_id or task_id
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0 NOT NULL,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    published BOOLEAN DEFAULT FALSE NOT NULL
);

-- Index for the Outbox Publisher to quickly fetch pending events using FOR UPDATE SKIP LOCKED
CREATE INDEX idx_outbox_unprocessed ON outbox_events(published, next_retry_at, created_at);
