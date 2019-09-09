/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.jul;

import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

/**
 * A {@link MemoryHandler} implementation that will get New Relic metadata synchronously for later usage.
 *
 * To use a {@link NewRelicMemoryHandler}, add it to your logging.properties and configure it like any other
 * {@link MemoryHandler}.
 *
 * <pre>{@code
 *     handlers = com.newrelic.logging.jul.NewRelicMemoryHandler
 *     com.newrelic.logging.jul.NewRelicMemoryHandler.target = java.util.logging.FileHandler
 * }</pre>
 */
public class NewRelicMemoryHandler extends MemoryHandler {
    @Override
    public synchronized void publish(LogRecord record) {
        super.publish(new NewRelicLogRecord(record));
    }
}
