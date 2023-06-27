/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.dropwizard;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.newrelic.api.agent.Agent;
import com.newrelic.logging.core.LogAsserts;
import com.newrelic.logging.logback.NewRelicAsyncAppender;
import io.dropwizard.logging.AbstractOutputStreamAppenderFactory;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.NullLevelFilterFactory;
import io.dropwizard.logging.layout.DropwizardLayoutFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class NewRelicAppenderFactoryTest {
    @SuppressWarnings("WeakerAccess")
    @TempDir
    Path tempDir;
    private AsyncAppender appender;
    private LoggingEvent event;
    private AbstractOutputStreamAppenderFactory<ILoggingEvent> appenderFactory;
    private PipedOutputStream outputStream;
    private BufferedReader bufferedReader;
    private String output;

    @Test
    @Timeout(3)
    void shouldWrapLogFormatConsoleAppenderCorrectly() throws Throwable {
        givenMockAgentData();
        givenOurConsoleAppenderFactory();
        givenOurAppenderFactoryHasLogFormatLayout();
        givenARedirectedAppender();
        givenALoggingEvent();
        whenTheEventIsAppended();
        thenMockAgentDataIsInTheMessage();
        thenLogFormatLayoutWasUsed();
    }

    @Test
    @Timeout(3)
    void shouldWrapLogFormatFileAppenderCorrectly() throws Throwable {
        givenMockAgentData();
        givenOurFileAppenderFactory();
        givenOurAppenderFactoryHasLogFormatLayout();
        givenARedirectedAppender();
        givenALoggingEvent();
        whenTheEventIsAppended();
        thenMockAgentDataIsInTheMessage();
        thenLogFormatLayoutWasUsed();
    }

    @Test
    @Timeout(3)
    void shouldWrapJsonConsoleAppenderCorrectly() throws Throwable {
        givenMockAgentData();
        givenOurConsoleAppenderFactory();
        givenOurAppenderFactoryHasJsonLayoutType();
        givenARedirectedAppender();
        givenALoggingEvent();
        whenTheEventIsAppended();
        thenMockAgentDataIsInTheMessage();
        thenJsonLayoutWasUsed();
    }

    @Test
    @Timeout(3)
    void shouldWrapJsonFileAppenderCorrectly() throws Throwable {
        givenMockAgentData();
        givenOurFileAppenderFactory();
        givenOurAppenderFactoryHasJsonLayoutType();
        givenARedirectedAppender();
        givenALoggingEvent();
        whenTheEventIsAppended();
        thenMockAgentDataIsInTheMessage();
        thenJsonLayoutWasUsed();
    }

    @Test
    @Timeout(3)
    void shouldAppendCallerDataToJsonCorrectly() throws Throwable {
        givenMockAgentData();
        givenOurFileAppenderFactory();
        givenOurAppenderFactoryHasJsonLayoutType();
        givenARedirectedAppender();
        givenALoggingEventWithCallerData();
        whenTheEventIsAppended();
        thenJsonLayoutWasUsed();
        thenTheCallerDataIsInTheMessage();
    }

    @Test
    @Timeout(3)
    void shouldAppendMDCArgsToJsonWhenEnabled() throws Throwable {
        givenMockAgentData();
        givenOurFileAppenderFactory();
        givenOurAppenderFactoryHasJsonLayoutType();
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
        givenOurFileAppenderFactory();
        givenOurAppenderFactoryHasJsonLayoutType();
        givenARedirectedAppender();
        givenALoggingEventWithMDCDisabled();
        whenTheEventIsAppended();
        thenJsonLayoutWasUsed();
        thenTheMDCFieldsAreInTheMessage(false);
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

    private void givenMockAgentData() {
        Agent mockAgent = Mockito.mock(Agent.class);
        Mockito.when(mockAgent.getLinkingMetadata()).thenReturn(ImmutableMap.of("some.key", "some.value"));
        NewRelicAsyncAppender.agentSupplier = () -> mockAgent;
    }

    private void givenOurFileAppenderFactory() {
        NewRelicFileAppenderFactory factory = new NewRelicFileAppenderFactory();

        // this output stream will be ignored, but we need it for configuration purposes.
        factory.setCurrentLogFilename(tempDir.resolve("log.txt").toString());
        factory.setArchive(false);
        appenderFactory = factory;
    }

    private void givenOurConsoleAppenderFactory() {
        appenderFactory = new NewRelicConsoleAppenderFactory();
    }

    private void givenOurAppenderFactoryHasLogFormatLayout() {
        appenderFactory.setLayout(new LogFormatLayoutFactory());
        appenderFactory.setLogFormat("%-5p : %m : %replace(%mdc{}){'NewRelic:', ''}%n");
    }

    private void givenOurAppenderFactoryHasJsonLayoutType() {
        appenderFactory.setLayout(new NewRelicJsonLayoutFactory());
    }

    private void givenALoggingEvent() {
        event = new LoggingEvent();
        event.setMessage("test_error_message");
        event.setLevel(Level.ERROR);
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
        // Enable MDC collection
        System.setProperty("newrelic.log_extension.add_mdc", "false");

        // Add MDC data
        MDC.put("contextKey1", "contextData1");
        MDC.put("contextKey2", "contextData2");
        MDC.put("contextKey3", "contextData3");

        givenALoggingEvent();
    }

    private void givenALoggingEventWithCallerData() {
        givenALoggingEvent();
        event.setCallerData(new StackTraceElement[] { new Exception().getStackTrace()[0] });
    }

    private void givenARedirectedAppender() {
        LoggerContext context = new LoggerContext();

        Appender<ILoggingEvent> baseAppender = appenderFactory.build(
                context,
                "app name",
                new DropwizardLayoutFactory(), // default, unused in this case
                new NullLevelFilterFactory<>(),
                new AsyncLoggingEventAppenderFactory() // default, our classes replace this argument.
        );

        assertNotNull(baseAppender);

        if (baseAppender instanceof AsyncAppender) {
            appender = ((AsyncAppender) baseAppender);
            appender.iteratorForAppenders().forEachRemaining(app -> {
                if (app instanceof OutputStreamAppender) {
                    ((OutputStreamAppender) app).setOutputStream(outputStream);
                }
            });
        } else {
            fail("Expected the appender (" + baseAppender.getClass() + ") to implement NewRelicAsyncAppender.");
        }
    }

    private void whenTheEventIsAppended() {
        appender.doAppend(event);
    }

    private void thenLogFormatLayoutWasUsed() throws IOException {
        assertEquals("ERROR : test_error_message : some.key=some.value\n", getOutput());
    }

    private void thenJsonLayoutWasUsed() throws IOException {
        assertTrue(getOutput().contains("\"message\":\"test_error_message\""));
        assertTrue(getOutput().contains("\"log.level\":\"ERROR\""));
        assertTrue(getOutput().contains("\"some.key\":\"some.value\""));

        JsonParser parser = new JsonFactory().createParser(getOutput());
        parser.setCodec(new ObjectMapper());
        assertTrue(parser.readValueAsTree() instanceof ObjectNode);
    }

    private void thenMockAgentDataIsInTheMessage() throws Throwable {
        assertTrue(
                getOutput().contains("some.key=some.value")
                        || getOutput().contains("\"some.key\":\"some.value\""),
                "Expected >>" + getOutput() + "<< to contain some.key to some.value"
        );
    }

    private void thenTheCallerDataIsInTheMessage() throws Throwable {
        JsonParser parser = new JsonFactory().createParser(getOutput());
        parser.setCodec(new ObjectMapper());
        ObjectNode objectNode = parser.readValueAsTree();
        assertEquals(this.getClass().getName(), objectNode.get("class.name").asText());
        assertEquals("givenALoggingEventWithCallerData", objectNode.get("method.name").asText());
        assertTrue(objectNode.get("line.number").isNumber());
    }

    private String getOutput() throws IOException {
        if (output == null) {
            output = bufferedReader.readLine() + "\n";
        }
        return output;
    }

    @BeforeEach
    void setUp() throws Exception {
        // Clear MDC data before each test
        MDC.clear();
        outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clear MDC data after each test
        MDC.clear();
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
}
