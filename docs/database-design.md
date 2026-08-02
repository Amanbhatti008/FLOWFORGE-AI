# Database Schema

PostgreSQL is used as the primary relational database. The schema is managed by Flyway.

## Tables
1. **users**: Stores user information (email, password hash, role).
2. **workflows**: Logical grouping of workflow designs.
3. **workflow_versions**: The actual DAG JSON definitions linked to a specific workflow.
4. **workflow_executions**: Instances of a running workflow.
5. **tasks**: Individual node executions linked to a `workflow_execution`.

## Relationships
- A `User` has many `Workflows`.
- A `Workflow` has many `WorkflowVersions`.
- A `WorkflowExecution` is tied to a specific `WorkflowVersion`.
- A `WorkflowExecution` has many `Tasks`.

All primary keys use UUIDv4 for distributed uniqueness.
