package com.flowforge.workflow.worker;

import com.flowforge.workflow.domain.Task;

public interface TaskExecutor {
    /**
     * Executes the specific logic for this task.
     * @param task the task to execute
     * @return true if successful, false otherwise
     * @throws Exception if execution fails in an unexpected way
     */
    boolean execute(Task task) throws Exception;

    /**
     * The type of task this executor supports.
     */
    String getType();
}
