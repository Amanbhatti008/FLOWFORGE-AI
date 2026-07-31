package com.flowforge.workflow.domain;

import com.flowforge.security.entity.User;
import com.flowforge.workflow.statemachine.WorkflowStatus;
import com.flowforge.workflow.statemachine.WorkflowStateMachine;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_executions")
@Getter
@Setter
public class WorkflowExecution {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_version_id", nullable = false)
    private WorkflowVersion workflowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status = WorkflowStatus.PENDING; // Enforced by CHECK constraint in DB

    public void setStatus(WorkflowStatus newStatus) {
        WorkflowStateMachine.transitionOrThrow(this.status, newStatus);
        this.status = newStatus;
    }

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (startedAt == null && (status == WorkflowStatus.RUNNING || status == WorkflowStatus.PENDING)) {
            startedAt = Instant.now();
        }
    }
}
