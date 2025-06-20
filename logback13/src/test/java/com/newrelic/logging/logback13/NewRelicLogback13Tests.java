/*
 * Copyright 2025. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.newrelic.logging.logback13.NewRelicAsyncAppender;
import com.newrelic.logging.logback13.NewRelicEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NewRelicLogback13Tests {

    private static Logger logger;
    private LoggerContext loggerContext;
    private ByteArrayOutputStream outputStream;

    NewRelicAsyncAppender appender;
    OutputStreamAppender<ILoggingEvent> delegateAppender;

    private static final String CONTEXT_PREFIX = "context.";

    @BeforeEach
    void setup() {
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();

        logger = loggerContext.getLogger("TestLogger");
        logger.setLevel(Level.DEBUG);

        outputStream = new ByteArrayOutputStream();

        NewRelicEncoder encoder = new NewRelicEncoder();
        encoder.setContext(loggerContext);
        encoder.start();

        delegateAppender = new OutputStreamAppender<>();
        delegateAppender.setContext(loggerContext);
        delegateAppender.setName("NR_TEST_DELEGATE_APPENDER");
        delegateAppender.setOutputStream(outputStream);
        delegateAppender.setEncoder(encoder);
        delegateAppender.setImmediateFlush(true);
        delegateAppender.start();

        appender = new NewRelicAsyncAppender();
        appender.setContext(loggerContext);
        appender.setName("NR_TEST_APPENDER");
        appender.addAppender(delegateAppender);
        appender.start();

        logger.addAppender(appender);
    }

    @AfterEach
    void teardown() {
        MDC.clear();
    }

    @Test
    void shouldWrapJsonConsoleAppenderCorrectly() throws InterruptedException, IOException {
        logger.info("Very interesting test message");
        Thread.sleep(100);
        String output = getLogOutput();

        assertTrue(logger.isInfoEnabled());
        assertTrue(output.contains("\"message\":\"Very interesting test message\""));
    }

    @Test
    void shouldAllWorkCorrectlyWithoutMDC() throws InterruptedException {
        logger.info("Very interesting test message, no MDC");
        Thread.sleep(100);
        String output = getLogOutput();

        assertTrue(output.contains("Very interesting test message, no MDC"));
        assertFalse(output.contains(CONTEXT_PREFIX));
    }

    @Test
    void shouldAppendCallerDataToJsonCorrectly() throws InterruptedException {
        appender.setIncludeCallerData(true);
        logger.info("Test message with Caller Data");

        Thread.sleep(100);
        String output = getLogOutput();

        assertTrue(output.contains("class.name"));
        assertTrue(output.contains("method.name"));
        assertTrue(output.contains("line.number"));
        assertTrue(output.contains("Test message with Caller Data"));
    }

    @Test
    void shouldAppendMDCArgsToJsonWhenEnabled() throws InterruptedException {
        MDC.put("userId", "user-123");
        MDC.put("sessionId", "session-456");

        logger.info("Logging with MDC enabled");
        Thread.sleep(100);
        String output = getLogOutput();

        assertTrue(output.contains("\"context.userId\":\"user-123\""));
        assertTrue(output.contains("\"context.sessionId\":\"session-456\""));
        assertTrue(output.contains("Logging with MDC enabled"));
    }

    @Test
    void shouldNotAppendMDCArgsToJsonWhenMDCIsDisabled() throws InterruptedException {
        NewRelicAsyncAppender.isNoOpMDC = true;
        MDC.put("userId", "user-123");
        MDC.clear();

        logger.info("Logging with MDC disabled");
        Thread.sleep(100);
        String output = getLogOutput();

        assertTrue(output.contains("Logging with MDC disabled"));
        assertFalse(output.contains("\"context.userId\":\"user-123\""));
        assertFalse(output.contains("NewRelic:"));
    }

    @Test
    void shouldSerializeExceptionStackTraceCorrectly() throws InterruptedException {
        Exception exception = new IllegalArgumentException("Test exception");

        logger.error("Logging a test exception", exception);
        Thread.sleep(100);
        String output = getLogOutput();

        assertTrue(output.contains("Logging a test exception"));
        assertTrue(output.contains("java.lang.IllegalArgumentException"));
        assertTrue(output.contains("Test exception"));
        assertTrue(output.contains("at "));
    }

    @Test
    void shouldIncludeMarkersInJsonOutput() throws InterruptedException {
        Marker marker = MarkerFactory.getMarker("TEST_MARKER");
        logger.info(marker, "Log message with marker");

        Thread.sleep(100);
        String output = getLogOutput();

        assertTrue(output.contains("\"marker\":[\"TEST_MARKER\"]"));
        assertTrue(output.contains("Log message with marker"));
    }

    @Test
    void shouldLogToMultipleAppendersCorrectly() throws InterruptedException {
        ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
        OutputStreamAppender<ILoggingEvent> appender1 = new OutputStreamAppender<>();
        appender1.setContext(loggerContext);
        appender1.setName("NR_TEST_APPENDER_1");
        appender1.setOutputStream(stream1);
        appender1.setEncoder(new NewRelicEncoder());
        appender1.start();

        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        OutputStreamAppender<ILoggingEvent> appender2 = new OutputStreamAppender<>();
        appender2.setContext(loggerContext);
        appender2.setName("NR_TEST_APPENDER_2");
        appender2.setOutputStream(stream2);
        appender2.setEncoder(new NewRelicEncoder());
        appender2.start();

        logger.addAppender(appender1);
        logger.addAppender(appender2);
        logger.info("Test message for multiple appenders");
        Thread.sleep(100);

        String output1 = stream1.toString();
        String output2 = stream2.toString();

        assertTrue(output1.contains("\"message\":\"Test message for multiple appenders\""));
        assertTrue(output2.contains("\"message\":\"Test message for multiple appenders\""));

        assertNotEquals("", output1);
        assertNotEquals("", output2);
    }

    @Test
    void shouldHandleNullOrEmptyMessagesGracefully() throws InterruptedException {
        logger.info(null);
        Thread.sleep(100);
        String output = getLogOutput();
        assertTrue(output.contains("\"message\":null"));
        assertTrue(output.contains("\"log.level\":\"INFO\""));

        logger.info("");
        Thread.sleep(100);
        output = getLogOutput();
        assertTrue(output.contains("\"message\":\"\""));
        assertTrue(output.contains("\"log.level\":\"INFO\""));
    }

    @Test
    void shouldLogDifferentLevelsCorrectly() throws InterruptedException {
        logger.debug("Debug message");
        logger.info("Info message");
        logger.warn("Warn message");
        logger.error("Error message");

        Thread.sleep(100);
        String output = getLogOutput();

        assertTrue(output.contains("\"message\":\"Info message\""));
        assertTrue(output.contains("\"message\":\"Warn message\""));
        assertTrue(output.contains("\"message\":\"Error message\""));
        assertTrue(output.contains("\"message\":\"Debug message\""));

        assertTrue(output.contains("\"log.level\":\"DEBUG\""));
        assertTrue(output.contains("\"log.level\":\"INFO\""));
        assertTrue(output.contains("\"log.level\":\"WARN\""));
        assertTrue(output.contains("\"log.level\":\"ERROR\""));
    }

    private String getLogOutput() {
        return outputStream.toString().trim();
    }

}