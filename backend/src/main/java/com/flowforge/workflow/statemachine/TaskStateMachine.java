package com.flowforge.workflow.statemachine;

public class TaskStateMachine {

    public static void transitionOrThrow(TaskStatus currentStatus, TaskStatus targetStatus) {
        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new IllegalStateException(
                    String.format("Illegal Task Status Transition: Cannot move from %s to %s", currentStatus, targetStatus)
            );
        }
    }

    private static boolean isValidTransition(TaskStatus currentStatus, TaskStatus targetStatus) {
        if (currentStatus == null) {
            return targetStatus == TaskStatus.PENDING || targetStatus == TaskStatus.SCHEDULED;
        }

        switch (currentStatus) {
            case PENDING:
                return targetStatus == TaskStatus.SCHEDULED;
            case SCHEDULED:
                return targetStatus == TaskStatus.QUEUED || targetStatus == TaskStatus.FAILED;
            case QUEUED:
                return targetStatus == TaskStatus.RUNNING;
            case RUNNING:
                return targetStatus == TaskStatus.SUCCESS || targetStatus == TaskStatus.FAILED || targetStatus == TaskStatus.SCHEDULED;
            case FAILED:
                return targetStatus == TaskStatus.SCHEDULED; // Retry scenario
            case SUCCESS:
                return false; // Terminal state, no outbound transitions allowed
            default:
                return false;
        }
    }
}
