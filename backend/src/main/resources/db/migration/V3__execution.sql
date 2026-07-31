-- Table for tracking workflow executions
CREATE TABLE workflow_executions (
    id UUID PRIMARY KEY,
    workflow_version_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL, -- e.g., RUNNING, SUCCESS, FAILED
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_workflow_executions_version FOREIGN KEY (workflow_version_id) REFERENCES workflow_versions(id),
    CONSTRAINT fk_workflow_executions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_workflow_executions_status CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED'))
);

CREATE INDEX idx_workflow_executions_status ON workflow_executions(status);
CREATE INDEX idx_workflow_executions_version ON workflow_executions(workflow_version_id);
