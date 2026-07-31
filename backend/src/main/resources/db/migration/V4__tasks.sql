-- Table for individual task instances within a workflow execution
CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    workflow_execution_id UUID NOT NULL,
    workflow_version_id UUID NOT NULL,
    task_ref_name VARCHAR(255) NOT NULL, -- Logical name in the DAG
    type VARCHAR(255) NOT NULL,
    input_data JSONB,
    output_data JSONB,
    status VARCHAR(50) NOT NULL, -- PENDING, SCHEDULED, RUNNING, SUCCESS, FAILED
    retry_count INTEGER NOT NULL DEFAULT 0,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_tasks_execution FOREIGN KEY (workflow_execution_id) REFERENCES workflow_executions(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_version FOREIGN KEY (workflow_version_id) REFERENCES workflow_versions(id) ON DELETE CASCADE,
    CONSTRAINT uq_tasks_ref UNIQUE (workflow_execution_id, task_ref_name),
    CONSTRAINT chk_tasks_status CHECK (status IN ('PENDING', 'SCHEDULED', 'QUEUED', 'RUNNING', 'SUCCESS', 'FAILED', 'RETRYING', 'CANCELLED'))
);

-- Index for the Scheduler to quickly find tasks using FOR UPDATE SKIP LOCKED
CREATE INDEX idx_task_scheduler ON tasks(status, scheduled_at);
CREATE INDEX idx_tasks_execution ON tasks(workflow_execution_id);

-- Table for representing the DAG edges (dependencies between tasks)
CREATE TABLE task_dependencies (
    parent_task_id UUID NOT NULL,
    child_task_id UUID NOT NULL,
    PRIMARY KEY (parent_task_id, child_task_id),
    CONSTRAINT fk_dependency_parent FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_dependency_child FOREIGN KEY (child_task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Index for Scheduler to resolve dependencies efficiently
CREATE INDEX idx_dependency_parent ON task_dependencies(parent_task_id);
CREATE INDEX idx_dependency_child ON task_dependencies(child_task_id);

-- Table for actual execution attempts (idempotent processing model)
CREATE TABLE task_execution (
    id UUID PRIMARY KEY,
    workflow_execution_id UUID NOT NULL,
    task_id UUID NOT NULL,
    attempt_number INTEGER NOT NULL,
    worker_id VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_task_execution_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_execution_workflow FOREIGN KEY (workflow_execution_id) REFERENCES workflow_executions(id) ON DELETE CASCADE,
    -- Core constraint to guarantee exactly-once business effect semantics through idempotent execution
    CONSTRAINT uq_task_execution UNIQUE (workflow_execution_id, task_id, attempt_number),
    CONSTRAINT chk_task_execution_status CHECK (status IN ('PENDING', 'SCHEDULED', 'QUEUED', 'RUNNING', 'SUCCESS', 'FAILED', 'RETRYING', 'CANCELLED'))
);

CREATE INDEX idx_task_execution_task ON task_execution(task_id);
