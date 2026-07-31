-- Table for workflow metadata
CREATE TABLE workflows (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_workflows_owner FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Table for workflow versions to ensure old executions don't break when definitions change
CREATE TABLE workflow_versions (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    definition_json JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_workflow_versions_workflow FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE,
    CONSTRAINT uq_workflow_version UNIQUE (workflow_id, version_number)
);

CREATE INDEX idx_workflows_owner ON workflows(created_by);
CREATE INDEX idx_workflow_versions_workflow ON workflow_versions(workflow_id);
