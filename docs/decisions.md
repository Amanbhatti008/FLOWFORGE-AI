# Architectural Decision Records (ADR)

## ADR 1: Kafka for Distributed Execution
**Context:** We needed a way to horizontally scale the workflow task execution without workers stepping on each other's toes, and handle node crashes gracefully.
**Decision:** Apache Kafka is used as the event backbone. `workflow-events` is the primary topic, with `-retry` and `-dlt` for Dead Letter Queues (DLQ) for fault tolerance.

## ADR 2: Redis for Distributed Locking
**Context:** Kafka guarantees at-least-once delivery, which can lead to duplicate task execution.
**Decision:** Redisson is used for distributed locking around task execution.

## ADR 3: Security & Rate Limiting
**Context:** Protecting the API from brute-forcing and token theft.
**Decision:** Implemented short-lived Access Tokens (15 min) and long-lived Refresh Tokens (7 days). Argon2id is used for strong password hashing. Bucket4j limits incoming requests.
