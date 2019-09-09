/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.jul;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;

import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

class NewRelicLogRecord extends LogRecord {
    private NewRelicLogRecord(Level level, String msg) {
        this(level, msg, agentSupplier.get().getLinkingMetadata());
    }

    NewRelicLogRecord(Level level, String msg, Map<String, String> traceData) {
        super(level, msg);
        this.traceData = traceData;
        threadName = Thread.currentThread().getName();
    }

    NewRelicLogRecord(LogRecord record) {
        this(record.getLevel(), record.getMessage());
        setMillis(record.getMillis());
        setLoggerName(record.getLoggerName());
        setParameters(record.getParameters());
        setResourceBundleName(record.getResourceBundleName());
        setSourceClassName(record.getSourceClassName());
        setSourceMethodName(record.getSourceMethodName());
        setThreadID(record.getThreadID());
    }

    private final String threadName;

    String getThreadName() {
        return threadName;
    }

    private final Map<String, String> traceData;

    Map<String, String> getTraceData() {
        return traceData;
    }

    private static final long serialVersionUID = 53723902839002534L;

    static Supplier<Agent> agentSupplier = NewRelic::getAgent;
}
