/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.log4j2;

import com.google.common.collect.ImmutableMap;
import com.newrelic.api.agent.Agent;
import com.newrelic.logging.core.LogAsserts;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.newrelic.logging.core.ElementName.ERROR_CLASS;
import static com.newrelic.logging.core.ElementName.ERROR_MESSAGE;
import static com.newrelic.logging.core.ElementName.ERROR_STACK;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
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
        System.out.println("Message output: " + output);
        assertTrue(output.matches(
                "\\{"
                        + "\"message\":\"Here's a message\","
                        + "\"timestamp\":\\d+,"
                        + "\"thread.name\":\"[^\"]+\","
                        + "\"log.level\":\"ERROR\","
                        + "\"logger.name\":\"logger-name\""
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
                        + "\"line.number\":\\d+"
                        + "}\\n"
        ));
    }

    @Test
    void shouldLayoutNewRelicMessage() {
        NewRelicLayout target = NewRelicLayout.factory();
        Log4jLogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(new NewRelicMessage("Here's a message"))
                .setLevel(Level.WARN)
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
                        + "\"log.level\":\"WARN\","
                        + "\"logger.name\":\"logger-name\","
                        + "\"class.name\":\"\\S+\","
                        + "\"method.name\":\"\\S+\","
                        + "\"line.number\":\\d+,"
                        + "\"some.key\":\"some.value\""
                        + "}\\n"
        ));
    }

    @Test
    void shouldRenderExceptionFieldsJustFine() throws Exception {
        NewRelicLayout target = NewRelicLayout.factory();
        Log4jLogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(new NewRelicMessage("Here's a message"))
                .setLevel(Level.WARN)
                .setLoggerName("logger-name")
                .setThrown(new Exception("~~ oops ~~"))
                .build();

        String output = target.toSerializable(event);

        LogAsserts.assertFieldValues(output, ImmutableMap.<String, Object>builder()
                .put(ERROR_CLASS, "java.lang.Exception")
                .put(ERROR_MESSAGE, "~~ oops ~~")
                .put(ERROR_STACK, Pattern.compile(".*LayoutTest\\.shouldRenderExceptionFieldsJustFine.*", Pattern.DOTALL))
                .build());
    }

    @BeforeEach
    void setUp() {
        Mockito.when(contextMockAgent.getLinkingMetadata()).thenReturn(null);
        Mockito.when(mockAgent.getLinkingMetadata()).thenReturn(ImmutableMap.of("some.key", "some.value"));
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(mockAgent);
    }

    @AfterAll
    static void tearDownClass() {
        NewRelicMessage.agentSupplier = cachedAgent;
    }

    @BeforeAll
    static void setUpClass() {
        mockAgent = Mockito.mock(Agent.class);
        contextMockAgent = Mockito.mock(Agent.class);
        NewRelicContextDataProvider.agentSupplier = () -> contextMockAgent;
        NewRelicMessage.agentSupplier = () -> mockAgent;
        cachedAgent = NewRelicMessage.agentSupplier;
    }

    private static Agent mockAgent;
    private static Agent contextMockAgent;
    private static Supplier<Agent> cachedAgent;
}
