package com.flowforge.workflow.statemachine;

public class WorkflowStateMachine {

    public static void transitionOrThrow(WorkflowStatus currentStatus, WorkflowStatus targetStatus) {
        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new IllegalStateException(
                    String.format("Illegal Workflow Status Transition: Cannot move from %s to %s", currentStatus, targetStatus)
            );
        }
    }

    private static boolean isValidTransition(WorkflowStatus currentStatus, WorkflowStatus targetStatus) {
        if (currentStatus == null) {
            return targetStatus == WorkflowStatus.PENDING;
        }

        switch (currentStatus) {
            case PENDING:
                return targetStatus == WorkflowStatus.RUNNING;
            case RUNNING:
                return targetStatus == WorkflowStatus.SUCCESS || targetStatus == WorkflowStatus.FAILED;
            case FAILED:
                return false; // Terminal state
            case SUCCESS:
                return false; // Terminal state
            default:
                return false;
        }
    }
}
