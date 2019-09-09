/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import com.newrelic.api.agent.Trace;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceMain {

    private static final Logger noAgentLogger = LogManager.getLogger("no_plugin_logging");
    private static final Logger agentLogger = LogManager.getLogger("newrelic_plugin_logging");

    private static final ExecutorService executors = Executors.newFixedThreadPool(8);

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: PerformanceMain <log-level> <identifier>");
            return;
        }

        String identifier = args[1];
        Level level = Level.getLevel(args[0].toUpperCase());
        System.out.println("Log level for performance run: " + level + ", identifier: " + identifier);

        // First, run without agent logging
        Collection<Future<?>> futures = new ArrayList<>();
        final AtomicInteger noAgentMessageCount = new AtomicInteger(0);
        for (int i = 0; i < 8; i++) {
            futures.add(executors.submit(() -> {
                int count = 0;

                long endTime = System.currentTimeMillis() + 60000;
                while (System.currentTimeMillis() < endTime) {
                    noAgentLoggerInfo(level, "Here is my message: " + System.currentTimeMillis());
                    count++;
                }

                noAgentMessageCount.addAndGet(count);
            }));
        }
        waitForFutures(futures);
        System.out.println("No Plugin Message Count: " + noAgentMessageCount.get() + " :: " + identifier);

        // Finally, run with agent logging
        futures = new ArrayList<>();
        final AtomicInteger agentMessageCount = new AtomicInteger(0);
        for (int i = 0; i < 8; i++) {
            futures.add(executors.submit(() -> {
                int count = 0;

                long endTime = System.currentTimeMillis() + 60000;
                while (System.currentTimeMillis() < endTime) {
                    agentLoggerInfo(level, "Here is my message: " + System.currentTimeMillis());
                    count++;
                }

                agentMessageCount.addAndGet(count);
            }));
        }
        waitForFutures(futures);
        System.out.println("With Plugin Message Count: " + agentMessageCount.get() + " :: " + identifier);

        executors.shutdown();
    }

    private static void waitForFutures(Collection<Future<?>> futures) throws InterruptedException, ExecutionException {
        for (Future<?> future : futures) {
            future.get();
        }
    }

    @Trace(dispatcher = true)
    private static void agentLoggerInfo(Level level, String message) {
        if (level == Level.INFO) {
            agentLogger.info(message);
        }
    }

    @Trace(dispatcher = true)
    private static void noAgentLoggerInfo(Level level, String message) {
        if (level == Level.INFO) {
            noAgentLogger.info(message);
        }
    }

}
