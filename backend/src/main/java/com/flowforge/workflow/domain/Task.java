package com.flowforge.workflow.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tasks", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_task_execution", columnNames = {"workflow_execution_id", "task_ref_name"})
})
@Getter
@Setter
public class Task {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_execution_id", nullable = false)
    private WorkflowExecution workflowExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_version_id", nullable = false)
    private WorkflowVersion workflowVersion;

    @Column(name = "task_ref_name", nullable = false)
    private String taskRefName;

    @Column(name = "type", nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private com.flowforge.workflow.statemachine.TaskStatus status;

    public void setStatus(com.flowforge.workflow.statemachine.TaskStatus newStatus) {
        com.flowforge.workflow.statemachine.TaskStateMachine.transitionOrThrow(this.status, newStatus);
        this.status = newStatus;
    }

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_data")
    private String inputData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_data")
    private String outputData;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
