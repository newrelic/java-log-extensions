/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.jul;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ElementName;
import com.newrelic.logging.core.ExceptionUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A {@link Formatter} that will render New Relic's JSON log format.
 *
 * To use a {@link NewRelicFormatter}, use it in the {@link java.util.logging.Handler#setFormatter(Formatter)}
 * call if you are configuring in code, or set it in the logging properties file like this:
 *
 * <pre>{@code
 * java.util.logging.FileHandler.pattern = ./logs/app-log-file.log
 * java.util.logging.FileHandler.formatter = com.newrelic.logging.jul.NewRelicFormatter
 * }</pre>
 */
public class NewRelicFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        StringWriter sw = new StringWriter();

        try (JsonGenerator generator = new JsonFactory().createGenerator(sw)) {
            writeToGenerator(record, generator);
        } catch (IOException e) {
            return e.toString();
        }

        return sw.toString() + "\n";
    }

    private void writeToGenerator(LogRecord record, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeObjectField(ElementName.MESSAGE, formatMessage(record));
        generator.writeObjectField(ElementName.TIMESTAMP, record.getMillis());
        generator.writeObjectField(ElementName.LOG_LEVEL, record.getLevel().toString());
        generator.writeObjectField(ElementName.LOGGER_NAME, record.getLoggerName());

        if (record.getSourceClassName() != null && record.getSourceMethodName() != null) {
            generator.writeObjectField(ElementName.CLASS_NAME, record.getSourceClassName());
            generator.writeObjectField(ElementName.METHOD_NAME, record.getSourceMethodName());
        }

        if (record instanceof NewRelicLogRecord) {
            generator.writeObjectField(ElementName.THREAD_NAME, ((NewRelicLogRecord) record).getThreadName());

            Map<String, String> traceData = ((NewRelicLogRecord) record).getTraceData();
            for (Map.Entry<String, String> traceEntry : traceData.entrySet()) {
                generator.writeStringField(traceEntry.getKey(), traceEntry.getValue());
            }
        }

        if (record.getThrown() != null) {
            generator.writeObjectField(ElementName.ERROR_CLASS, record.getThrown().getClass().getName());
            generator.writeObjectField(ElementName.ERROR_MESSAGE, record.getThrown().getMessage());
            generator.writeObjectField(ElementName.ERROR_STACK, ExceptionUtil.getErrorStack(record.getThrown()));
        }

        generator.writeEndObject();
    }
}
