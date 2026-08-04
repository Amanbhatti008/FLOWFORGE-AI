ALTER TABLE workflows ADD COLUMN cron_expression VARCHAR(100);
ALTER TABLE workflows ADD COLUMN next_run_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_workflows_next_run_at ON workflows(next_run_at);
