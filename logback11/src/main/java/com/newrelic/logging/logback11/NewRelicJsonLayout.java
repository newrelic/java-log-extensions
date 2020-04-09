/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback11;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ElementName;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class NewRelicJsonLayout extends LayoutBase<ILoggingEvent> {

	@Override
    public String doLayout(ILoggingEvent event) {
        StringWriter sw = new StringWriter();

        try (JsonGenerator generator = new JsonFactory().createGenerator(sw)) {
            writeToGenerator(event, generator);
        } catch (Throwable ignored) {
            return event.getFormattedMessage();
        }

        sw.append('\n');
        return sw.toString();
    }

    private void writeToGenerator(ILoggingEvent event, JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeStringField(ElementName.MESSAGE, event.getFormattedMessage());
        generator.writeNumberField(ElementName.TIMESTAMP, event.getTimeStamp());
        generator.writeStringField(ElementName.LOG_LEVEL, event.getLevel().toString());
        generator.writeStringField(ElementName.LOGGER_NAME, event.getLoggerName());
        generator.writeStringField(ElementName.THREAD_NAME, event.getThreadName());

        if (event.hasCallerData()) {
            StackTraceElement element = event.getCallerData()[event.getCallerData().length - 1];
            generator.writeStringField(ElementName.CLASS_NAME, element.getClassName());
            generator.writeStringField(ElementName.METHOD_NAME, element.getMethodName());
            generator.writeNumberField(ElementName.LINE_NUMBER, element.getLineNumber());
        }

        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        for (Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                generator.writeStringField(entry.getKey(), entry.getValue());
            }
        }

        generator.writeEndObject();
    }
}
