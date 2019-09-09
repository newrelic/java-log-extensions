/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.google.common.collect.ImmutableMap;
import com.newrelic.api.agent.Agent;
import com.newrelic.logging.core.LogAsserts;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewRelicLogbackTests {
    private AsyncAppender appender;
    private LoggingEvent event;
    private PipedOutputStream outputStream;
    private BufferedReader bufferedReader;
    private String output;

    @Test
    @Timeout(3)
    void shouldWrapJsonConsoleAppenderCorrectly() throws Throwable {
        givenMockAgentData();
        givenARedirectedAppender();
        givenALoggingEvent();
        whenTheEventIsAppended();
        thenMockAgentDataIsInTheMessage();
        thenJsonLayoutWasUsed();
    }

    @Test
    @Timeout(3)
    void shouldAllWorkCorrectlyEvenWithoutMDC() throws Throwable {
        givenMockAgentData();
        givenARedirectedAppender();
        givenMDCIsANoOp();
        givenALoggingEvent();
        whenTheEventIsAppended();
        thenMockAgentDataIsInTheMessage();
        thenJsonLayoutWasUsed();
    }

    @Test
    @Timeout(3)
    void shouldAppendCallerDataToJsonCorrectly() throws Throwable {
        givenMockAgentData();
        givenARedirectedAppender();
        givenALoggingEventWithCallerData();
        whenTheEventIsAppended();
        thenJsonLayoutWasUsed();
        thenTheCallerDataIsInTheMessage();
    }

    private void givenMockAgentData() {
        Agent mockAgent = Mockito.mock(Agent.class);
        Mockito.when(mockAgent.getLinkingMetadata()).thenReturn(ImmutableMap.of("some.key", "some.value"));
        NewRelicAsyncAppender.agentSupplier = () -> mockAgent;
    }

    private void givenMDCIsANoOp() {
        NewRelicAsyncAppender.isNoOpMDC = true;
    }

    private void givenALoggingEvent() {
        event = new LoggingEvent();
        event.setMessage("test_error_message");
        event.setLevel(Level.ERROR);
    }

    private void givenALoggingEventWithCallerData() {
        givenALoggingEvent();
        event.setCallerData(new StackTraceElement[] { new Exception().getStackTrace()[0] });
    }

    private void givenARedirectedAppender() {
        NewRelicEncoder encoder = new NewRelicEncoder();
        encoder.start();

        LoggerContext context = new LoggerContext();
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();
        // must be set _after_ start()
        consoleAppender.setOutputStream(outputStream);

        appender = new NewRelicAsyncAppender();
        appender.setContext(context);
        appender.addAppender(consoleAppender);
        appender.start();
    }

    private void whenTheEventIsAppended() {
        appender.doAppend(event);
    }

    private void thenJsonLayoutWasUsed() throws IOException {
        LogAsserts.assertFieldValues(
                getOutput(),
                ImmutableMap.of(
                        "message", "test_error_message",
                        "log.level", "ERROR",
                        "some.key", "some.value"
                )
        );
    }

    private void thenMockAgentDataIsInTheMessage() throws Throwable {
        assertTrue(
                getOutput().contains("some.key=some.value")
                        || getOutput().contains("\"some.key\":\"some.value\""),
                "Expected >>" + getOutput() + "<< to contain some.key to some.value"
        );
    }

    private void thenTheCallerDataIsInTheMessage() throws Throwable {
        LogAsserts.assertFieldValues(
                getOutput(),
                ImmutableMap.of("class.name", this.getClass().getName(), "method.name", "givenALoggingEventWithCallerData")
        );
    }

    private String getOutput() throws IOException {
        if (output == null) {
            output = bufferedReader.readLine() + "\n";
        }
        assertNotNull(output);
        return output;
    }

    @BeforeEach
    void setUp() throws Exception {
        isNoOpMDC = NewRelicAsyncAppender.isNoOpMDC;
        outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws Exception {
        NewRelicAsyncAppender.isNoOpMDC = isNoOpMDC;
        appender.stop();
        appender.detachAndStopAllAppenders();
        outputStream.close();
        bufferedReader.close();
    }

    @BeforeAll
    static void setUpClass() {
        savedSupplier = NewRelicAsyncAppender.agentSupplier;
    }

    @AfterAll
    static void tearDownClass() {
        NewRelicAsyncAppender.agentSupplier = savedSupplier;
    }

    private static Supplier<Agent> savedSupplier;
    private boolean isNoOpMDC;

}
