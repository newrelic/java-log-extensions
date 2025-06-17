/*
 * Copyright 2025 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.logback13;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;
import org.slf4j.MDC;
import org.slf4j.helpers.NOPMDCAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.newrelic.logging.core.LogExtensionConfig.CONTEXT_PREFIX;

/**
 * An {@link AsyncAppender} implementation that synchronously captures New Relic trace data.
 * <p>
 * This appender will wrap the existing {@link AsyncAppender} logic in order to capture New Relic data on
 * the same thread as the log message was created. To use, wrap your existing appender in your config xml,
 * and use the async appender in the appropriate logger.
 *
 * <pre>{@code
 *      <appender name="ASYNC" class="com.newrelic.logging.logback13.NewRelicAsyncAppender">
 *          <appender-ref ref="LOG_FILE" />
 *      </appender>
 *
 *      <root level="INFO">
 *          <appender-ref ref="ASYNC" />
 *      <root>
 * }</pre>
 *
 * @see <a href="https://logback.qos.ch/manual/appenders.html#AsyncAppender">Logback AsyncAppender</a>
 */
public class NewRelicAsyncAppender extends AsyncAppender {
    public static final String NEW_RELIC_PREFIX = "NewRelic:";

    @Override
    protected void preprocess(ILoggingEvent eventObject) {
        eventObject.prepareForDeferredProcessing();
        if (isIncludeCallerData()) {
            eventObject.getCallerData();
        }
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }

        Map<String, String> combinedContextMap = new HashMap<>();

        Map<String, String> copyMdc = MDC.getMDCAdapter().getCopyOfContextMap();
        if (copyMdc != null) {
            for (Map.Entry<String, String> entry : copyMdc.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    combinedContextMap.put(CONTEXT_PREFIX + entry.getKey(), entry.getValue());
                }
            }
        }

        Map<String, String> linkingMetadata = agentSupplier.get().getLinkingMetadata();
        for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
            combinedContextMap.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
        }

        if (!isNoOpMDC) {
            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
                MDC.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }
            super.append(eventObject);
        } else {
            ILoggingEvent wrappedEvent = new CustomLoggingEventWrapper(eventObject, combinedContextMap);
            super.append(wrappedEvent);
        }
    }

    //visible for testing
    public static Supplier<Agent> agentSupplier = NewRelic::getAgent;

    // visible for testing
    @SuppressWarnings("WeakerAccess")
    public static boolean isNoOpMDC = MDC.getMDCAdapter() instanceof NOPMDCAdapter;
}

