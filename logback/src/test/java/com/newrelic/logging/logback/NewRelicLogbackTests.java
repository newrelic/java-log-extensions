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
import ch.qos.logback.classic.spi.ThrowableProxy;
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
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        givenALoggingEventWithMDCDisabled();
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

    @Test
    @Timeout(3)
    void shouldAppendErrorDataCorrectly() throws Throwable {
        givenMockAgentData();
        givenARedirectedAppender();
        givenALoggingEventWithExceptionData();
        whenTheEventIsAppended();
        thenJsonLayoutWasUsed();
        thenTheExceptionDataIsInTheMessage();
    }

    @Test
    @Timeout(3)
    void shouldAppendFullErrorDataCorrectly() throws Throwable {
        givenMockAgentData();
        givenARedirectedAppender();
        givenALoggingEventWithExceptionDataIncludingCausedBy();
        whenTheEventIsAppended();
        thenJsonLayoutWasUsed();
        thenTheExceptionCausedByDataIsInTheMessage();
    }

    @Test
    @Timeout(3)
    void shouldAppendCustomArgsToJsonCorrectly() throws Throwable {
        givenMockAgentData();
        givenARedirectedAppender();
        givenALoggingEventWithCustomArgs();
        whenTheEventIsAppended();
        thenJsonLayoutWasUsed();
        thenTheCustomArgsAreInTheMessage();
    }

    @Test
    @Timeout(3)
    void shouldAppendMDCArgsToJsonWhenEnabled() throws Throwable {
        givenMockAgentData();
        givenARedirectedAppender();
        givenALoggingEventWithMDCEnabled();
        whenTheEventIsAppended();
        thenJsonLayoutWasUsed();
        thenTheMDCFieldsAreInTheMessage(true);
    }

    @Test
    @Timeout(3)
    void shouldNotAppendMDCArgsToJsonWhenDisabled() throws Throwable {
        givenMockAgentData();
        givenARedirectedAppender();
        givenALoggingEventWithMDCDisabled();
        whenTheEventIsAppended();
        thenJsonLayoutWasUsed();
        thenTheMDCFieldsAreInTheMessage(false);
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

    private void givenALoggingEventWithExceptionData() {
        givenALoggingEvent();
        event.setThrowableProxy(new ThrowableProxy(new Exception("~~ oops ~~")));
    }

    private void givenALoggingEventWithExceptionDataIncludingCausedBy() {
        System.setProperty("newrelic.log_extension.include_full_error_stack", "true");
        givenALoggingEvent();
        event.setThrowableProxy(new ThrowableProxy(new Exception("~~ oops ~~", new Exception("~~ oops inner 1 ~~", new Exception("~~ oops inner 2 ~~")))));
    }

    private void givenALoggingEventWithCallerData() {
        givenALoggingEvent();
        event.setCallerData(new StackTraceElement[] { new Exception().getStackTrace()[0] });
    }

    private void givenALoggingEventWithCustomArgs() {
        givenALoggingEvent();
        CustomArgument customArgument1 = new CustomArgument("customKey1", "customValue1");
        CustomArgument customArgument2 = new CustomArgument("customKey2", "customValue2");
        Object[] customArgs = new Object[2];
        customArgs[0] = customArgument1;
        customArgs[1] = customArgument2;
        event.setArgumentArray(customArgs);
    }

    private void givenALoggingEventWithMDCEnabled() {
        // Enable MDC collection
        System.setProperty("newrelic.log_extension.add_mdc", "true");

        // Add MDC data
        MDC.put("contextKey1", "contextData1");
        MDC.put("contextKey2", "contextData2");
        MDC.put("contextKey3", "contextData3");

        givenALoggingEvent();
    }

    private void givenALoggingEventWithMDCDisabled() {
        // Disable MDC collection
        System.setProperty("newrelic.log_extension.add_mdc", "false");

        // Add MDC data
        MDC.put("contextKey1", "contextData1");
        MDC.put("contextKey2", "contextData2");
        MDC.put("contextKey3", "contextData3");

        givenALoggingEvent();
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

    private void thenTheExceptionDataIsInTheMessage() throws Throwable {
        LogAsserts.assertFieldValues(
                getOutput(),
                ImmutableMap.of(
                        "error.class", "java.lang.Exception",
                        "error.stack", Pattern.compile(".*NewRelicLogbackTests\\.shouldAppendErrorDataCorrectly.*", Pattern.DOTALL),
                        "error.message", "~~ oops ~~")
        );
    }

    private void thenTheExceptionCausedByDataIsInTheMessage() throws Throwable {
        LogAsserts.assertFieldValues(
                getOutput(),
                ImmutableMap.of(
                        "error.stack.causedby", Pattern.compile(".*Caused By.*", Pattern.DOTALL),
                        "error.stack.inner1", Pattern.compile(".*oops inner 1.*", Pattern.DOTALL),
                        "error.stack.inner2", Pattern.compile(".*oops inner 2.*", Pattern.DOTALL)
        ));
    }

    private void thenTheCustomArgsAreInTheMessage() throws Throwable {
        LogAsserts.assertFieldValues(
                getOutput(),
                ImmutableMap.of("customKey1", "customValue1", "customKey2", "customValue2")
        );
    }

    private void thenTheMDCFieldsAreInTheMessage(boolean shouldExist) throws Throwable {
        String result = getOutput();
        boolean contextKey1Exists = LogAsserts.assertFieldExistence(
                "context.contextKey1",
                result,
                shouldExist
        );
        assertEquals(shouldExist, contextKey1Exists, "MDC context.contextKey1 should exist: " + shouldExist);

        boolean contextKey2Exists = LogAsserts.assertFieldExistence(
                "context.contextKey2",
                result,
                shouldExist
        );
        assertEquals(shouldExist, contextKey2Exists, "MDC context.contextKey2 should exist: " + shouldExist);

        boolean contextKey3Exists = LogAsserts.assertFieldExistence(
                "context.contextKey3",
                result,
                shouldExist
        );
        assertEquals(shouldExist, contextKey3Exists, "MDC context.contextKey3 should exist: " + shouldExist);
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
        // Clear MDC data before each test
        MDC.clear();
        isNoOpMDC = NewRelicAsyncAppender.isNoOpMDC;
        outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clear MDC data before each test
        MDC.clear();
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
