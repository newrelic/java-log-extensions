/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.forwarder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class to provide thread factories generators
 */
public class Threads {

    private Threads() {}

    private static final ConcurrentHashMap<String, AtomicInteger> threadCounters = new ConcurrentHashMap<>();

    public static ThreadFactory daemonNamedThreadFactory(String threadName) {
        final int threadNumber = threadCounters.computeIfAbsent(threadName, k -> new AtomicInteger()).incrementAndGet();
        final String numberedName = threadName + "-" + threadNumber;

        return runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName(numberedName);
            thread.setDaemon(true);
            return thread;
        };
    }

}
