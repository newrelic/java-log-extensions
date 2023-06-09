/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ElementName;
import com.newrelic.logging.core.ExceptionUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.newrelic.logging.core.LogExtensionConfig.getMaxStackSize;

public class NewRelicJsonLayout extends LayoutBase<ILoggingEvent> {
    private final Integer maxStackSize;

    public NewRelicJsonLayout() {
        this(getMaxStackSize());
    }

    public NewRelicJsonLayout(Integer maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        StringWriter sw = new StringWriter();

        try (JsonGenerator generator = JsonFactoryProvider.getInstance().createGenerator(sw)) {
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

        if (event.getMarker() != null) {
            generator.writeStringField(ElementName.MARKER, event.getMarker().getName());
        }

        Object[] customArgumentArray = event.getArgumentArray();
        if (customArgumentArray != null) {
            for (Object oneCustomArgumentObject : customArgumentArray) {
                if (oneCustomArgumentObject instanceof CustomArgument) {
                    CustomArgument customArgument = (CustomArgument) oneCustomArgumentObject;
                    generator.writeStringField(customArgument.getKey(), customArgument.getValue());
                }
            }
        }

        IThrowableProxy proxy = event.getThrowableProxy();
        if (proxy != null) {
            generator.writeObjectField(ElementName.ERROR_CLASS, proxy.getClassName());
            generator.writeObjectField(ElementName.ERROR_MESSAGE, proxy.getMessage());

            StackTraceElementProxy[] stackProxy = proxy.getStackTraceElementProxyArray();
            if (stackProxy != null && stackProxy.length > 0) {
                List<StackTraceElement> elements = new ArrayList<>(maxStackSize);
                for (int i = 0; i < maxStackSize && i < stackProxy.length; i++) {
                    elements.add(stackProxy[i].getStackTraceElement());
                }

                generator.writeObjectField(ElementName.ERROR_STACK, ExceptionUtil.getErrorStack(elements.toArray(new StackTraceElement[0]), maxStackSize));
            }
        }

        generator.writeEndObject();
    }
}
