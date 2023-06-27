/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.logback;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;
import org.slf4j.MDC;
import org.slf4j.helpers.NOPMDCAdapter;

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
 *     <appender name="ASYNC" class="com.newrelic.logging.logback.NewRelicAsyncAppender">
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

    @Override
    protected void preprocess(ILoggingEvent eventObject) {
        Map<String, String> linkingMetadata = agentSupplier.get().getLinkingMetadata();
        // NR linking metadata is added to the MDC map
        if (!isNoOpMDC) {
            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
                MDC.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }
        }
        super.preprocess(eventObject);
        if (isNoOpMDC) {
            for (Map.Entry<String, String> entry : linkingMetadata.entrySet()) {
                /*
                 * This only works if there is at least one entry in the MDC map. If the MDC map is empty when
                 * calling eventObject.getMDCPropertyMap() it simply returns Collections.emptyMap() which is
                 * immutable. Calling put() on the immutable map causes a java.lang.UnsupportedOperationException
                 * which results in the NR linking metadata never being added.
                 */
                eventObject.getMDCPropertyMap().put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }
        }
    }

    //visible for testing
    public static Supplier<Agent> agentSupplier = NewRelic::getAgent;
    @SuppressWarnings("WeakerAccess") //visible for testing
    static boolean isNoOpMDC = MDC.getMDCAdapter() instanceof NOPMDCAdapter;
}
