# System Architecture

## Overview
FlowForge AI is a distributed workflow orchestration platform built using a microservices-inspired monolithic core. It relies heavily on asynchronous event-driven architecture.

## Core Components
- **API Server (Spring Boot)**: Handles HTTP requests, triggers workflows, serves UI endpoints, and provides WebSocket updates.
- **Workflow Engine (Spring Boot / Kafka)**: The state machine logic is decoupled using Apache Kafka. The engine publishes tasks to topics.
- **Task Workers**: Consumers listen to Kafka topics, acquire distributed locks via Redis, and execute node tasks.
- **Data Persistence**: PostgreSQL is used for relational data (workflows, executions, user data).
- **In-Memory Cache**: Redis handles distributed locks and session-like transient states.

## Request Flow
1. User submits a workflow via REST API.
2. `WorkflowTriggerService` parses the DAG and pushes initial tasks to Kafka (`workflow-events`).
3. A `TaskWorkerService` consumes the task, locks it in Redis to prevent concurrent execution, and runs it.
4. On success, `DagProgressionService` evaluates the DAG for downstream nodes and schedules them.
5. On failure, tasks are sent to `workflow-events-retry` and subsequently to a dead letter queue (`workflow-events-dlt`).
