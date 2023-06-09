/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.log4j2;

import com.google.common.collect.ImmutableMap;
import com.newrelic.api.agent.Agent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Supplier;

import static com.newrelic.logging.core.LogExtensionConfig.ADD_MDC_SYS_PROP;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LayoutTest {
    @Test
    void shouldLayoutStandardMessage() {
        NewRelicLayout target = NewRelicLayout.factory();
        Log4jLogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(new ParameterizedMessage("Here's a message"))
                .setLevel(Level.ERROR)
                .setLoggerName("logger-name")
                .build();

        String output = target.toSerializable(event);
        assertTrue(output.matches(
                "\\{"
                        + "\"message\":\"Here's a message\","
                        + "\"timestamp\":\\d+,"
                        + "\"thread.name\":\"[^\"]+\","
                        + "\"log.level\":\"ERROR\","
                        + "\"logger.name\":\"logger-name\","
                        + "\"some.key\":\"some.value\""
                        + "}\\n"
        ));
    }

    @Test
    void shouldLayoutStandardMessageWithCallData() {
        NewRelicLayout target = NewRelicLayout.factory();
        Log4jLogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(new ParameterizedMessage("Here's a message"))
                .setLevel(Level.ERROR)
                .setLoggerName("logger-name")
                .setSource(new Exception().getStackTrace()[0])
                .setIncludeLocation(true)
                .build();

        String output = target.toSerializable(event);
        assertTrue(output.matches(
                "\\{"
                        + "\"message\":\"Here's a message\","
                        + "\"timestamp\":\\d+,"
                        + "\"thread.name\":\"[^\"]+\","
                        + "\"log.level\":\"ERROR\","
                        + "\"logger.name\":\"logger-name\","
                        + "\"class.name\":\"\\S+\","
                        + "\"method.name\":\"\\S+\","
                        + "\"line.number\":\\d+,"
                        + "\"some.key\":\"some.value\""
                        + "}\\n"
        ));
    }

    @Test
    void testLayoutMDCDisabledByDefault() {
        NewRelicLayout target = NewRelicLayout.factory();

        // Add MDC data
        ThreadContext.put("contextKey1", "contextData1");
        ThreadContext.put("contextKey2", "contextData2");
        ThreadContext.put("contextKey3", "contextData3");

        Log4jLogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(new ParameterizedMessage("Here's a message"))
                .setLevel(Level.ERROR)
                .setLoggerName("logger-name")
                .build();

        String output = target.toSerializable(event);
        // MDC is disabled by default so shouldn't be included in the layout
        assertTrue(output.matches(
                "\\{"
                        + "\"message\":\"Here's a message\","
                        + "\"timestamp\":\\d+,"
                        + "\"thread.name\":\"[^\"]+\","
                        + "\"log.level\":\"ERROR\","
                        + "\"logger.name\":\"logger-name\","
                        + "\"some.key\":\"some.value\""
                        + "}\\n"
        ));

        ThreadContext.clearAll();
    }

    @Test
    void testLayoutMDCExplicitlyDisabled() {
        // Explicitly disable MDC
        System.setProperty(ADD_MDC_SYS_PROP, "false");

        NewRelicLayout target = NewRelicLayout.factory();

        // Add MDC data
        ThreadContext.put("contextKey1", "contextData1");
        ThreadContext.put("contextKey2", "contextData2");
        ThreadContext.put("contextKey3", "contextData3");

        Log4jLogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(new ParameterizedMessage("Here's a message"))
                .setLevel(Level.ERROR)
                .setLoggerName("logger-name")
                .build();

        String output = target.toSerializable(event);
        // MDC is explicitly disabled so shouldn't be included in the layout
        assertTrue(output.matches(
                "\\{"
                        + "\"message\":\"Here's a message\","
                        + "\"timestamp\":\\d+,"
                        + "\"thread.name\":\"[^\"]+\","
                        + "\"log.level\":\"ERROR\","
                        + "\"logger.name\":\"logger-name\","
                        + "\"some.key\":\"some.value\""
                        + "}\\n"
        ));

        ThreadContext.clearAll();
    }

    @Test
    void testLayoutMDCExplicitlyEnabled() {
        // Explicitly enable MDC
        System.setProperty(ADD_MDC_SYS_PROP, "true");

        NewRelicLayout target = NewRelicLayout.factory();

        // Add MDC data
        ThreadContext.put("contextKey1", "contextData1");
        ThreadContext.put("contextKey2", "contextData2");
        ThreadContext.put("contextKey3", "contextData3");

        Log4jLogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(new ParameterizedMessage("Here's a message"))
                .setLevel(Level.ERROR)
                .setLoggerName("logger-name")
                .build();

        String output = target.toSerializable(event);
        // MDC is enabled so should be included in the layout
        assertTrue(output.matches(
                "\\{"
                        + "\"message\":\"Here's a message\","
                        + "\"timestamp\":\\d+,"
                        + "\"thread.name\":\"[^\"]+\","
                        + "\"log.level\":\"ERROR\","
                        + "\"logger.name\":\"logger-name\","
                        + "\"some.key\":\"some.value\","
                        + "\"contextKey2\":\"contextData2\","
                        + "\"contextKey3\":\"contextData3\","
                        + "\"contextKey1\":\"contextData1\""
                        + "}\\n"
        ));

        ThreadContext.clearAll();
    }

    @BeforeEach
    void setUp() {
        Mockito.when(mockAgent.getLinkingMetadata()).thenReturn(ImmutableMap.of("some.key", "some.value"));
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(mockAgent);
    }

    @AfterAll
    static void tearDownClass() {
        NewRelicContextDataProvider.agentSupplier = cachedAgent;
    }

    @BeforeAll
    static void setUpClass() {
        mockAgent = Mockito.mock(Agent.class);
        NewRelicContextDataProvider.agentSupplier = () -> mockAgent;
        cachedAgent = NewRelicContextDataProvider.agentSupplier;
    }

    private static Agent mockAgent;
    private static Supplier<Agent> cachedAgent;
}
