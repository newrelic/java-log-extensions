/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ElementName;
import com.newrelic.logging.core.ExceptionUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.newrelic.logging.core.ExceptionUtil.MAX_STACK_SIZE;

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
        generator.writeObjectField(ElementName.MESSAGE, event.getFormattedMessage());
        generator.writeObjectField(ElementName.TIMESTAMP, event.getTimeStamp());
        generator.writeObjectField(ElementName.LOG_LEVEL, event.getLevel().toString());
        generator.writeObjectField(ElementName.LOGGER_NAME, event.getLoggerName());
        generator.writeObjectField(ElementName.THREAD_NAME, event.getThreadName());

        if (event.hasCallerData()) {
            StackTraceElement element = event.getCallerData()[event.getCallerData().length - 1];

            generator.writeObjectField(ElementName.CLASS_NAME, element.getClassName());
            generator.writeObjectField(ElementName.METHOD_NAME, element.getMethodName());
            generator.writeObjectField(ElementName.LINE_NUMBER, element.getLineNumber());
        }

        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        for (Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                generator.writeStringField(entry.getKey(), entry.getValue());
            }
        }

        IThrowableProxy proxy = event.getThrowableProxy();
        if (proxy != null) {
            generator.writeObjectField(ElementName.ERROR_CLASS, proxy.getClassName());
            generator.writeObjectField(ElementName.ERROR_MESSAGE, proxy.getMessage());

            StackTraceElementProxy[] stackProxy = proxy.getStackTraceElementProxyArray();
            if (stackProxy != null && stackProxy.length > 0) {
                List<StackTraceElement> elements = new ArrayList<>(MAX_STACK_SIZE);
                for (int i = 0; i < MAX_STACK_SIZE && i < stackProxy.length; i++) {
                    elements.add(stackProxy[i].getStackTraceElement());
                }

                generator.writeObjectField(ElementName.ERROR_STACK, ExceptionUtil.getErrorStack(elements.toArray(new StackTraceElement[0])));
            }
        }

        generator.writeEndObject();
    }
}
