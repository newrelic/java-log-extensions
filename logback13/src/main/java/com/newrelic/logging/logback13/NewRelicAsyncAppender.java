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
        eventObject.prepareForDeferredProcessing();
//        Map<String, String> linkingMetadata = agentSupplier.get().getLinkingMetadata();
//        Map<String, String> mdcCopy = MDC.getMDCAdapter().getCopyOfContextMap();
//        Map<String, String> combinedContextMap = new HashMap<>();
//        ILoggingEvent wrappedEvent;
//        System.out.println("[NewRelicAsyncAppender.preprocess] linkingMetadata = agentSupplier.get().getLinkingMetadata() : " + linkingMetadata.entrySet());
//        System.out.println("[NewRelicAsyncAppender.preprocess] mdcCopy = eventObject.getMDCPropertyMap() : " + mdcCopy.entrySet());
//
//        if (!isNoOpMDC) {
//            combinedContextMap.putAll(linkingMetadata);
//
//            for (Map.Entry<String, String> entry : mdcCopy.entrySet()) {
//                combinedContextMap.put(CONTEXT_PREFIX + entry.getKey(), entry.getValue());
//            }
//            System.out.println("[NewRelicAsyncAppender.preprocess] combinedContextMap after adding CONTEXT_PREFIX: " + combinedContextMap);
//
//            wrappedEvent = new CustomLoggingEventWrapper(eventObject, combinedContextMap);
//            super.preprocess(wrappedEvent);
//        }
//
//        /*
//         * In logback-1.3.x, calling eventObject.getMDCPropertyMap() returns an immutable map.
//         * To add New Relic linking metadata to the event, we need to pull the existing ContextMap and set it as the MDCPropertyMap.
//         * This allows us to maintain compatibility with logback-1.3.x while still supporting the New Relic linking metadata
//         * in the event object.
//         */
//        if (isNoOpMDC) {
//            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
//                combinedContextMap.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
//            }
//            wrappedEvent = new CustomLoggingEventWrapper(eventObject, combinedContextMap);
//            super.preprocess(wrappedEvent);
//        }
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }

        Map<String, String> baseMdc = MDC.getMDCAdapter().getCopyOfContextMap();
        Map<String, String> combinedContextMap = new HashMap<>();

        for (Map.Entry<String, String> entry : baseMdc.entrySet()) {
            combinedContextMap.put(CONTEXT_PREFIX + entry.getKey(), entry.getValue());
        }

        Map<String, String> linkingMetadata = agentSupplier.get().getLinkingMetadata();
        combinedContextMap.putAll(linkingMetadata);
        System.out.println("[NewRelicAsyncAppender.append] combinedContextMap: " + combinedContextMap.entrySet());

        ILoggingEvent wrappedEvent = new CustomLoggingEventWrapper(eventObject, combinedContextMap);

        System.out.println("[NewRelicAsyncAppender.append] wrappedEvent: " + wrappedEvent.getMDCPropertyMap().entrySet());

        super.append(wrappedEvent);
    }

    //visible for testing
    public static Supplier<Agent> agentSupplier = NewRelic::getAgent;

    // visible for testing
    @SuppressWarnings("WeakerAccess")
    public static boolean isNoOpMDC = MDC.getMDCAdapter() instanceof NOPMDCAdapter;
}

