/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.log4j2;

import com.google.common.collect.ImmutableMap;
import com.newrelic.api.agent.Agent;
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
        NewRelicMessage.agentSupplier = cachedAgent;
    }

    @BeforeAll
    static void setUpClass() {
        mockAgent = Mockito.mock(Agent.class);
        NewRelicMessage.agentSupplier = () -> mockAgent;
        cachedAgent = NewRelicMessage.agentSupplier;
    }

    private static Agent mockAgent;
    private static Supplier<Agent> cachedAgent;
}
