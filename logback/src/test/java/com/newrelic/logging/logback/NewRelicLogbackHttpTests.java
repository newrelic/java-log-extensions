/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.logback;

import static com.newrelic.logging.logback.NewRelicHttpAppender.PLUGIN_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.newrelic.api.agent.Agent;

import com.newrelic.api.agent.Config;
import com.newrelic.logging.core.ElementName;
import com.newrelic.logging.forwarder.LogForwarderConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.newrelic.telemetry.logs.Log;

import com.newrelic.logging.forwarder.LogForwarder;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewRelicLogbackHttpTests {
    private static final long TIMESTAMP = 1234L;
    private static final Throwable CAUSE = new Throwable("a_throwable_cause_message");
    private static final Throwable THROWABLE = new Throwable("a_throwable_message", CAUSE);

    private NewRelicHttpAppender appender;
    private LoggingEvent event;
    private final List<Log> logs = new ArrayList<>();
    private LogForwarder forwarder;

    @Test
    @Timeout(3)
    void shouldWorkCorrectly() throws Throwable {
        givenTheStartedAppender();
        givenALoggingEvent();
        whenTheEventIsAppended();
        thenLogForwarderIsCalled();
        thenLogAttributesAreSetCorrectly();
    }

    @Test
    @Timeout(3)
    void shouldConfigureTheForwarderCorrectly() throws Throwable {
        givenTheStartedAppenderWithCustomConfigurationParameters();
        thenLogForwarderIsCreatedWithCustomConfiguration();
    }

    private void givenTheStartedAppenderWithCustomConfigurationParameters() {
        givenTheAppender();
        appender.setEndpoint("https://a-custom-endpoint/log/v1");
        appender.setLicense("a-custom-license");
        appender.setMaxQueuedLogs(1_000);
        appender.setMaxLogsPerBatch(100);
        appender.setMaxTerminationTimeSeconds(600);
        appender.start();
    }

    private void thenLogForwarderIsCreatedWithCustomConfiguration() {
        ArgumentCaptor<LogForwarderConfiguration> capturedConfiguration = ArgumentCaptor.forClass(LogForwarderConfiguration.class);
        Mockito.verify(appender).createForwarder(eq(PLUGIN_TYPE), capturedConfiguration.capture());
        assertEquals("https://a-custom-endpoint/log/v1", capturedConfiguration.getValue().getEndpoint());
        assertEquals("a-custom-license", capturedConfiguration.getValue().getLicense());
        assertEquals(1_000, capturedConfiguration.getValue().getMaxQueuedLogs());
        assertEquals(100, capturedConfiguration.getValue().getMaxLogsPerBatch());
        assertEquals(600, capturedConfiguration.getValue().getMaxTerminationTimeSeconds());
        // TODO: Add other configurations
    }

    private void givenTheStartedAppender() {
        givenTheAppender();
        appender.start();
    }

    private void givenTheAppender() {
        appender = Mockito.spy(new NewRelicHttpAppender());
        forwarder = Mockito.mock(LogForwarder.class);
        Mockito.doReturn(forwarder).when(appender).createForwarder(eq(PLUGIN_TYPE), any());
        LoggerContext context = new LoggerContext();
        appender.setContext(context);
    }

    private void givenALoggingEvent() {
        event = new LoggingEvent();
        event.setTimeStamp(TIMESTAMP);
        event.setThreadName("a-thread-name");
        event.setMessage("a-test-error-message");
        event.setLevel(Level.ERROR);
        event.setLoggerName("a-logger-name");
        Map<String, String> mdcMap = new HashMap<>();
        mdcMap.put("some.mdc.key", "an-mdc-value");
        event.setMDCPropertyMap(mdcMap);
        event.setThrowableProxy(new ThrowableProxy(THROWABLE));
    }

    private void whenTheEventIsAppended() {
        appender.doAppend(event);
    }

    private void thenLogForwarderIsCalled() {
        ArgumentCaptor<Log> capturedLog = ArgumentCaptor.forClass(Log.class);
        Mockito.verify(forwarder).append(capturedLog.capture());
        logs.add(capturedLog.getValue());
    }

    private void thenLogAttributesAreSetCorrectly() throws Throwable {
        assertEquals(1, logs.size());
        assertEquals(TIMESTAMP, logs.get(0).getTimestamp());
        assertEquals("ERROR", logs.get(0).getLevel());
        assertEquals("a-test-error-message", logs.get(0).getMessage());
        assertEquals("a-thread-name", logs.get(0).getAttributes().asMap().get(ElementName.THREAD_NAME));
        assertEquals("a-logger-name", logs.get(0).getAttributes().asMap().get(ElementName.LOGGER_NAME));
        assertEquals("an-agent-value", logs.get(0).getAttributes().asMap().get("some.agent.key"));
        assertEquals("an-mdc-value", logs.get(0).getAttributes().asMap().get("some.mdc.key"));
        assertEquals(THROWABLE, logs.get(0).getThrowable());
        assertEquals(CAUSE, logs.get(0).getThrowable().getCause());
    }

    private void mockAgent() {
        // Agent mock
        Config config = Mockito.mock(Config.class);
        Agent agent = Mockito.mock(Agent.class);
        Mockito.when(config.getValue(eq("license_key"))).thenReturn("a-sample-license-key");
        Mockito.when(agent.getConfig()).thenReturn(config);

        // Agent mocked attributes
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("some.agent.key", "an-agent-value");
        Mockito.when(agent.getLinkingMetadata()).thenReturn(attributesMap);

        // Replace agent supplier
        NewRelicHttpAppender.agentSupplier = () -> agent;
    }

    @BeforeEach
    void init() {
        mockAgent();
    }

    @AfterEach
    void tearDown() throws Exception {
        logs.clear();
        appender.stop();
    }
}
