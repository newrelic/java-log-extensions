/*
 * Copyright 2025 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.logback13;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;
import org.slf4j.MDC;
import org.slf4j.helpers.NOPMDCAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * An {@link AsyncAppender} implementation that synchronously captures New Relic trace data.
 * <p>
 * This appender will wrap the existing {@link AsyncAppender} logic in order to capture New Relic data
 * on the same thread as the log message was created. To use, wrap your existing appender in your
 * config xml, and use the async appender in the appropriate logger.
 *
 * <pre>{@code
 *     <appender name="ASYNC" class="com.newrelic.logging.logback1.NewRelicAsyncAppender">
 *         <appender-ref ref="LOG_FILE" />
 *     </appender>
 *
 *     <root level="INFO">
 *         <appender-ref ref="ASYNC" />
 *     <root>
 * }</pre>
 *
 * @see <a href="https://logback.qos.ch/manual/appenders.html#AsyncAppender">Logback AsyncAppender</a>
 */
public class NewRelicAsyncAppender extends AsyncAppender {
    public static final String NEW_RELIC_PREFIX = "NewRelic:";

    // required for logback-1.3.x compatibility
    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void preprocess(ILoggingEvent eventObject) {
        Map<String, String> linkingMetadata = agentSupplier.get().getLinkingMetadata();

        // NR linking metadata is added to the MDC map, if MDC is enabled
        if (!isNoOpMDC) {
            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
                MDC.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }
        }
        super.preprocess(eventObject);
        /*
         * In logback-1.3.x, calling eventObject.getMDCPropertyMap() returns an immutable map (Collections.emptyMap()).
         * To add New Relic linking metadata to the event, we first check if MDC is disabled (isNoOpMDC). Then we create
         * a new MDC map (that is mutable) and add the linking metadata and call setMDCPropertyMap() to set the new map.
         */
        if (isNoOpMDC) {
            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
                eventObject.getMDCPropertyMap().put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }
        }
    }

    //visible for testing
    public static Supplier<Agent> agentSupplier = NewRelic::getAgent;
    @SuppressWarnings("WeakerAccess") //visible for testing
    public static boolean isNoOpMDC = MDC.getMDCAdapter() instanceof NOPMDCAdapter;
}

