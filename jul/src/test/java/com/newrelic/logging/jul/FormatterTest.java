/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.jul;

import com.google.common.collect.ImmutableMap;
import com.newrelic.logging.core.LogAsserts;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import static com.newrelic.logging.core.ElementName.CLASS_NAME;
import static com.newrelic.logging.core.ElementName.ERROR_CLASS;
import static com.newrelic.logging.core.ElementName.ERROR_MESSAGE;
import static com.newrelic.logging.core.ElementName.ERROR_STACK;
import static com.newrelic.logging.core.ElementName.LOGGER_NAME;
import static com.newrelic.logging.core.ElementName.LOG_LEVEL;
import static com.newrelic.logging.core.ElementName.MESSAGE;
import static com.newrelic.logging.core.ElementName.METHOD_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FormatterTest {
    @Test
    void shouldEscapeSpecialCharactersJustFine() {
        LogRecord record = new LogRecord(Level.FINE, "\" \\ \n");

        NewRelicFormatter target = new NewRelicFormatter();
        String result = target.format(record);

        int index = result.indexOf(MESSAGE + "\":\"") + 10;
        String actual = result.substring(index, index + 8);
        String expected = "\\\" \\\\ \\n";
        assertEquals(expected, actual);
    }

    @Test
    void shouldRenderStandardLogMessagesJustFine() throws IOException {
        LogRecord record = new LogRecord(Level.FINE, "Here's a message");
        record.setLoggerName(getClass().getName());
        record.setSourceClassName("source-class");
        record.setSourceMethodName("source-method");

        NewRelicFormatter target = new NewRelicFormatter();
        String result = target.format(record);

        LogAsserts.assertFieldValues(result, ImmutableMap.<String, Object>builder()
                .put(MESSAGE, "Here's a message")
                .put(LOG_LEVEL, "FINE")
                .put(LOGGER_NAME, getClass().getName())
                .put(CLASS_NAME, "source-class")
                .put(METHOD_NAME, "source-method")
                .build());
    }

    @Test
    void shouldRenderExceptionFieldsJustFine() throws IOException {
        LogRecord record = new LogRecord(Level.FINE, "Here's a throwable");
        record.setThrown(new Exception("~~ oops ~~"));

        NewRelicFormatter target = new NewRelicFormatter();
        String result = target.format(record);

        LogAsserts.assertFieldValues(result, ImmutableMap.<String, Object>builder()
                .put(ERROR_CLASS, "java.lang.Exception")
                .put(ERROR_MESSAGE, "~~ oops ~~")
                .put(ERROR_STACK, Pattern.compile(".*FormatterTest\\.shouldRenderExceptionFieldsJustFine.*", Pattern.DOTALL))
                .build());
    }

    @Test
    void shouldRenderTraceDataCorrectly() throws IOException {
        Map<String, String> td = ImmutableMap.of("an.opaque", "value", "another.opaque", "another-value");

        LogRecord record = new NewRelicLogRecord(Level.WARNING, "my-message", td);
        record.setSourceClassName("source-class");
        record.setSourceMethodName("source-method");

        NewRelicFormatter target = new NewRelicFormatter();
        String result = target.format(record);

        LogAsserts.assertFieldValues(result, ImmutableMap.<String, Object>builder()
                .put(MESSAGE, "my-message")
                .put(LOG_LEVEL, "WARNING")
                .put("an.opaque", "value")
                .put("another.opaque", "another-value")
                .build());
    }

}
