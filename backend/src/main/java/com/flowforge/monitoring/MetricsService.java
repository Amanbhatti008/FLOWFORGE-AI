package com.flowforge.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter workflowExecutionTotal;
    private final Counter kafkaMessagesProcessed;
    private final Counter failedJobs;
    private final Timer workflowExecutionTime;
    private final Timer apiLatency;

    public MetricsService(MeterRegistry registry) {
        this.workflowExecutionTotal = Counter.builder("workflow_execution_total")
                .description("Total number of workflow executions started")
                .register(registry);

        this.kafkaMessagesProcessed = Counter.builder("kafka_messages_processed")
                .description("Total number of Kafka messages processed by workers")
                .register(registry);

        this.failedJobs = Counter.builder("failed_jobs")
                .description("Total number of jobs that failed and reached terminal state")
                .register(registry);

        this.workflowExecutionTime = Timer.builder("workflow_execution_time")
                .description("Time taken to execute a full workflow")
                .register(registry);

        this.apiLatency = Timer.builder("api_latency")
                .description("Latency of API requests")
                .register(registry);
    }

    public void incrementWorkflowExecution() {
        workflowExecutionTotal.increment();
    }

    public void incrementKafkaMessagesProcessed() {
        kafkaMessagesProcessed.increment();
    }

    public void incrementFailedJobs() {
        failedJobs.increment();
    }

    public void recordWorkflowExecutionTime(long durationMillis) {
        workflowExecutionTime.record(durationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    
    public void recordApiLatency(long durationMillis) {
        apiLatency.record(durationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
