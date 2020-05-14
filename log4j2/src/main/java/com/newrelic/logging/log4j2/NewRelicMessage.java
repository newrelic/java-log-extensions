/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.log4j2;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.util.Map;
import java.util.function.Supplier;

/**
 * A {@link ParameterizedMessage} extension that adds New Relic trace data synchronously.
 *
 * To capture New Relic trace data along with a log message, the trace and span must be captured at
 * the time the message is logged. This class accommodates that. Create an instance of this class
 * instead of {@link ParameterizedMessage} or {@link org.apache.logging.log4j.message.Message} directly.
 *
 * @deprecated Use NewRelicContextProvider instead. This class has two serious problems:
 * 1. It only works with ParameterizedMessages.
 * 2. It obtains the trace data even for Messages that will never be published causing unnecessary overhead.
 */
public class NewRelicMessage extends ParameterizedMessage {
    private final Map<String, String> traceData;

    public NewRelicMessage(String messagePattern, Object[] arguments, Throwable throwable) {
        super(messagePattern, arguments, throwable);
        traceData = agentSupplier.get().getLinkingMetadata();
    }

    public NewRelicMessage(String messagePattern, Object... arguments) {
        super(messagePattern, arguments);
        traceData = agentSupplier.get().getLinkingMetadata();
    }

    Map<String, String> getTraceData() {
        return traceData;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        if (o.getClass() != getClass()) {
            return false;
        }

        NewRelicMessage other = (NewRelicMessage) o;

        if (traceData == null) {
            return other.traceData == null;
        }

        return traceData.equals(other.traceData);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + traceData.hashCode();
    }

    private static final long serialVersionUID = -665938495738490697L;

    //visible for testing
    static Supplier<Agent> agentSupplier = NewRelic::getAgent;
}
