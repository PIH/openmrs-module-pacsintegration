package org.openmrs.module.pacsintegration.runner;

import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;

public class DaemonTaskRunner implements TaskRunner {

    private static DaemonToken daemonToken;

    @Override
    public void run(Runnable runnable) {
        Daemon.runInDaemonThreadAndWait(runnable, daemonToken);
    }

    public static void setDaemonToken(DaemonToken daemonToken) {
        DaemonTaskRunner.daemonToken = daemonToken;
    }
}
