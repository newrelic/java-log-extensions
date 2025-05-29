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
    protected void append(ILoggingEvent eventObject) {
        Map<String, String> linkingMetadata = agentSupplier.get().getLinkingMetadata();
        if (!isNoOpMDC) {
            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
                MDC.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }
        }
        super.append(eventObject);
    }

    @Override
    protected void preprocess(ILoggingEvent eventObject) {
        Map<String, String> linkingMetadata = agentSupplier.get().getLinkingMetadata();

        if (!isNoOpMDC) {
            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
                MDC.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }

            Map<String, String> mdcCopy = MDC.getCopyOfContextMap();
            if (mdcCopy != null && eventObject instanceof LoggingEvent) {
                ((LoggingEvent) eventObject).setMDCPropertyMap(mdcCopy);
            }

            super.preprocess(eventObject);
            /*
             * In logback-1.3.x, calling eventObject.getMDCPropertyMap() returns an immutable map (Collections.emptyMap()).
             * To add New Relic linking metadata to the event, we need to set the argument array with the linking metadata.
             * This allows us to maintain compatibility with logback-1.3.x while still supporting the New Relic linking metadata
             * in the event object.
             */
        }

        if (isNoOpMDC) {
            Object[] args = linkingMetadata.entrySet().stream()
                    .map(event -> CustomArgument.keyValue(event.getKey(), event.getValue()))
                    .toArray(CustomArgument[]::new);
            ((LoggingEvent) eventObject).setArgumentArray(args);

            Map<String, String> mdcMap = new HashMap<>();
            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
                mdcMap.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }
            ((LoggingEvent) eventObject).setMDCPropertyMap(mdcMap);
        }
    }

    //visible for testing
    public static Supplier<Agent> agentSupplier = NewRelic::getAgent;

    // visible for testing
    @SuppressWarnings("WeakerAccess")
    public static boolean isNoOpMDC = MDC.getMDCAdapter() instanceof NOPMDCAdapter;
}

