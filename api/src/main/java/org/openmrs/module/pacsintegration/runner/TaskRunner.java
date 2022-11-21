package org.openmrs.module.pacsintegration.runner;

/**
 * This interface allows for different implementations to be defined that can be used to execute code
 * Those implementations may choose to run in the same thread, or a different thread, or to ensure that there
 * is an authenticated session started and stopped around the execution of the task.
 */
public interface TaskRunner {

    void run(Runnable runnable);

}
