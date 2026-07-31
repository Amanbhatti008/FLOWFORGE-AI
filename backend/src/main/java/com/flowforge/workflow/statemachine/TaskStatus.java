package com.flowforge.workflow.statemachine;

public enum TaskStatus {
    PENDING,
    SCHEDULED,
    QUEUED,
    RUNNING,
    SUCCESS,
    FAILED,
    RETRYING,
    CANCELLED
}
