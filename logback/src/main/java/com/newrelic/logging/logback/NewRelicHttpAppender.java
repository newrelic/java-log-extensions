/*
 * Copyright 2021 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import com.newrelic.logging.forwarder.LogForwarder;
import com.newrelic.logging.forwarder.LogForwarderConfiguration;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.logs.Log;

import static com.newrelic.logging.forwarder.LogForwarderConfiguration.DEFAULT_FLUSH_INTERVAL_SECONDS;
import static com.newrelic.logging.forwarder.LogForwarderConfiguration.DEFAULT_LICENSE;
import static com.newrelic.logging.forwarder.LogForwarderConfiguration.DEFAULT_MAX_QUEUED_LOGS;
import static com.newrelic.logging.forwarder.LogForwarderConfiguration.DEFAULT_MAX_LOGS_PER_BATCH;
import static com.newrelic.logging.forwarder.LogForwarderConfiguration.DEFAULT_MAX_SCHEDULED_LOGS_TO_BE_APPENDED;
import static com.newrelic.logging.forwarder.LogForwarderConfiguration.DEFAULT_MAX_TERMINATION_TIME_SECONDS;
import static com.newrelic.logging.forwarder.LogForwarderConfiguration.DEFAULT_URL;

import java.util.Optional;
import java.util.function.Supplier;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.logging.core.ElementName;

/**
 * An {@link AppenderBase} implementation that will prepare the logs to be forwarded to NewRelic and add them to a log
 * forwarder that will do the forwarding asynchronously.
 *
 * <pre>{@code
 *     <appender name="HTTP" class="com.newrelic.logging.logback.NewRelicHttpAppender">
 *     </appender>
 *
 *     <root level="INFO">
 *         <appender-ref ref="HTTP" />
 *     <root>
 * }</pre>
 * <p>
 * For more configuration/parameter options @see LogForwarderConfiguration class and add them to your appender as for
 * example:
 *
 * <pre>{@code
 *     <appender name="HTTP" class="com.newrelic.logging.logback.NewRelicHttpAppender">
 *         <endpoint>https://log-api.eu.newrelic.com/log/v1</endpoint>
 *         <license>YOUR_LICENSE_KEY</license>
 *         <maxQueuedLogs>1000000</maxQueuedLogs>
 *         <maxLogsPerBatch>100000</maxLogsPerBatch>
 *         [...]
 *     </appender>
 * }</pre>
 * <p>
 * NOTE: License key will default to the one set on the agent configuration, but you can override it.
 */
public class NewRelicHttpAppender extends AppenderBase<ILoggingEvent> {

    private LogForwarder forwarder;
    protected static String PLUGIN_TYPE = "nr-java-logback";

    protected String endpoint = DEFAULT_URL;
    protected String license = DEFAULT_LICENSE;
    protected int maxQueuedLogs = DEFAULT_MAX_QUEUED_LOGS;
    protected int maxLogsPerBatch = DEFAULT_MAX_LOGS_PER_BATCH;
    protected int maxTerminationTimeSeconds = DEFAULT_MAX_TERMINATION_TIME_SECONDS;
    protected int flushIntervalSeconds = DEFAULT_FLUSH_INTERVAL_SECONDS;
    protected int maxScheduledLogsToBeAppended = DEFAULT_MAX_SCHEDULED_LOGS_TO_BE_APPENDED;

    public NewRelicHttpAppender() {
        setName(NewRelicHttpAppender.class.getCanonicalName());
    }

    public void setEndpoint(String givenEndpoint) {
        endpoint = givenEndpoint;
    }

    public void setLicense(String givenLicense) {
        license = givenLicense;
    }

    public void setMaxQueuedLogs(int givenMaxQueuedLogs) {
        maxQueuedLogs = givenMaxQueuedLogs;
    }

    public void setMaxLogsPerBatch(int givenMaxLogsPerBatch) {
        maxLogsPerBatch = givenMaxLogsPerBatch;
    }

    public void setMaxTerminationTimeSeconds(int givenMaxTerminationTimeSeconds) {
        maxTerminationTimeSeconds = givenMaxTerminationTimeSeconds;
    }

    public void setFlushIntervalSeconds(int givenFlushIntervalSeconds) {
        flushIntervalSeconds = givenFlushIntervalSeconds;
    }

    public void setMaxScheduledLogsToBeAppended(int givenMaxScheduledLogsToBeAppended) {
        maxScheduledLogsToBeAppended = givenMaxScheduledLogsToBeAppended;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        forwarder.append(encode(eventObject));
    }

    private Log encode(ILoggingEvent event) {
        final Attributes attributes = new Attributes();

        attributes.put(ElementName.THREAD_NAME, event.getThreadName());
        attributes.put(ElementName.LOGGER_NAME, event.getLoggerName());

        agentSupplier.get().getLinkingMetadata().forEach(attributes::put);

        event.getMDCPropertyMap().forEach(attributes::put);

        Optional.ofNullable(event.getArgumentArray()).ifPresent(customArgumentArray -> {
            for (Object oneCustomArgumentObject : customArgumentArray) {
                if (oneCustomArgumentObject instanceof CustomArgument) {
                    CustomArgument customArgument = (CustomArgument) oneCustomArgumentObject;
                    attributes.put(customArgument.getKey(), customArgument.getValue());
                }
            }
        });

        final Log.LogBuilder builder = Log.builder()
                .message(event.getFormattedMessage())
                .level(event.getLevel().toString())
                .timestamp(event.getTimeStamp())
                .attributes(attributes);

        Optional
                .ofNullable((ThrowableProxy) event.getThrowableProxy())
                .ifPresent(proxy -> builder.throwable(proxy.getThrowable()));

        return builder.build();
    }

    @Override
    public void start() {
        super.start();
        forwarder = createForwarder(PLUGIN_TYPE, generateLogForwarderConfiguration());
        forwarder.start();
    }

    @Override
    public void stop() {
        super.stop();
        forwarder.shutdown();
    }

    private LogForwarderConfiguration generateLogForwarderConfiguration() {
        return LogForwarderConfiguration.builder()
                .setEndpoint(endpoint)
                .setLicense(license)
                .setMaxQueuedLogs(maxQueuedLogs)
                .setMaxScheduledLogsToBeAppended(maxScheduledLogsToBeAppended)
                .setMaxLogsPerBatch(maxLogsPerBatch)
                .setMaxTerminationTimeSeconds(maxTerminationTimeSeconds)
                .setFlushIntervalSeconds(flushIntervalSeconds)
                .build();
    }

    protected LogForwarder createForwarder(String givenPluginType, LogForwarderConfiguration configuration) {
        return new LogForwarder(givenPluginType, configuration);
    }

    //visible for testing
    public static Supplier<Agent> agentSupplier = NewRelic::getAgent;
}
