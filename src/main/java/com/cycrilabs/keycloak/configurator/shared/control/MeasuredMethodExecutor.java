package com.cycrilabs.keycloak.configurator.shared.control;

import io.quarkus.logging.Log;

public class MeasuredMethodExecutor {
    public static void measureExecutionTime(final Runnable method, final String methodName) {
        final long startTime = System.nanoTime();
        method.run();
        final long endTime = System.nanoTime();
        final long duration = endTime - startTime;
        Log.infof("%s finished in %sms", methodName, Long.valueOf(duration / 1_000_000));
    }
}
