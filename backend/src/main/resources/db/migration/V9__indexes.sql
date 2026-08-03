
CREATE INDEX IF NOT EXISTS idx_workflow_created_by ON workflows(created_by);

-- Indexes for WorkflowExecution table
CREATE INDEX IF NOT EXISTS idx_execution_status ON workflow_executions(status);
CREATE INDEX IF NOT EXISTS idx_execution_workflow_id ON workflow_executions(workflow_id);

-- Indexes for Tasks table
CREATE INDEX IF NOT EXISTS idx_tasks_execution_id ON tasks(execution_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
