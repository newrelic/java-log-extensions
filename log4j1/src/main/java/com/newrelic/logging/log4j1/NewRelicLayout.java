/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.log4j1;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ElementName;
import com.newrelic.logging.core.ExceptionUtil;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.io.IOException;
import java.io.StringWriter;

/**
 * A {@link Layout} that writes the New Relic JSON format.
 *
 * This layout must be added to an {@link Appender} via {@link org.apache.log4j.Appender#setLayout(Layout)} or properties.
 * The New Relic layout has specific fields and field names and has no customizable elements. To configure,
 * update your logging config xml like this:
 *
 * <pre>{@code
 *     <File name="MyFile" fileName="logs/app-log-file.log">
 *         <NewRelicLayout/>
 *     </File>
 * }</pre>
 *
 * @see <a href="https://logging.apache.org/log4j/2.x/manual/appenders.html#FileAppender">The FileAppender, for example</a>
 */
public class NewRelicLayout extends Layout {
    @Override
    public String format(LoggingEvent event) {
        StringWriter sw = new StringWriter();

        try (JsonGenerator generator = new JsonFactory().createGenerator(sw)) {
            writeToGenerator(event, generator);
        } catch (IOException e) {
            return e.toString();
        }

        return sw.toString() + "\n";
    }

    @SuppressWarnings("unchecked") // log4j1 does not use generics, so there's an unchecked cast.
    private void writeToGenerator(LoggingEvent event, final JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeObjectField(ElementName.MESSAGE, event.getRenderedMessage());
        generator.writeObjectField(ElementName.TIMESTAMP, event.getTimeStamp());
        generator.writeObjectField(ElementName.THREAD_NAME, event.getThreadName());
        generator.writeObjectField(ElementName.LOG_LEVEL, event.getLevel().toString());
        generator.writeObjectField(ElementName.LOGGER_NAME, event.getLoggerName());

        if (event.getLocationInformation() != null
                && !event.getLocationInformation().getClassName().equals(LocationInfo.NA)) {
            generator.writeObjectField(ElementName.CLASS_NAME, event.getLocationInformation().getClassName());
            generator.writeObjectField(ElementName.METHOD_NAME, event.getLocationInformation().getMethodName());
            generator.writeObjectField(ElementName.LINE_NUMBER, event.getLocationInformation().getLineNumber());
        }

        event.getProperties().forEach((key, value) -> {
            if (value != null) {
                try {
                    generator.writeStringField(key.toString(), value.toString());
                } catch (IOException ignored) {
                }
            }

        });

        if (event instanceof NewRelicLoggingEvent) {
            ((NewRelicLoggingEvent)event).getLinkedMetadata().forEach((key, value) -> {
                try {
                    generator.writeStringField(key, value);
                } catch (IOException ignored) {
                }
            });
        }

        if (event.getThrowableInformation() != null && event.getThrowableInformation().getThrowable() != null) {
            Throwable throwable = event.getThrowableInformation().getThrowable();
            generator.writeObjectField(ElementName.ERROR_CLASS, throwable.getClass().getName());
            generator.writeObjectField(ElementName.ERROR_MESSAGE, throwable.getMessage());
            generator.writeObjectField(ElementName.ERROR_STACK, ExceptionUtil.getErrorStack(throwable));
        }

        generator.writeEndObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ignoresThrowable() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activateOptions() { }
}
