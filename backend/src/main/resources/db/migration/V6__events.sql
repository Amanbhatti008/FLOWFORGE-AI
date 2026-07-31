-- Table for Event Sourcing Lite (Workflow Audit History)
CREATE TABLE workflow_events (
    event_id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL, -- The workflow_execution_id or task_id
    event_type VARCHAR(100) NOT NULL, -- e.g., TASK_STARTED, WORKFLOW_FAILED
    payload JSONB, -- Context data like {taskId:"123", worker:"worker-5"}
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Index for retrieving the event timeline of a specific aggregate (e.g., workflow execution)
CREATE INDEX idx_workflow_events_aggregate ON workflow_events(aggregate_id, created_at);
