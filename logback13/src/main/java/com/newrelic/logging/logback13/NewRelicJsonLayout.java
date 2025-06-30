/*
 * Copyright 2025. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback13;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ElementName;
import com.fasterxml.jackson.core.JsonFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.NOPMDCAdapter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * A custom layout that formats {@link ILoggingEvent} log events as JSON objects.
 * Adds standard log fields and enriches logs with linking metadata.
 *
 * This layout also adds MDC (Mapped Diagnostic Context) values using prefixed keys (e.g., "context.someKey:someValue").
 */

public class NewRelicJsonLayout extends LayoutBase<ILoggingEvent> {
    private boolean started = false;
    private Context context;

    @Override
    public String doLayout(ILoggingEvent eventObject) {
        StringWriter sw = new StringWriter();

        try {
            JsonGenerator generator = new JsonFactory().createGenerator(sw);
            writeToGenerator(eventObject, generator);
            generator.close();
        } catch (IOException ignored) {
            return eventObject.getFormattedMessage();
        }
        sw.append("\n");
        return sw.toString();
    }

    private void writeToGenerator(ILoggingEvent eventObject, JsonGenerator generator) throws IOException {

        generator.writeStartObject();
        generator.writeStringField(ElementName.MESSAGE, eventObject.getFormattedMessage());
        generator.writeNumberField(ElementName.TIMESTAMP, eventObject.getTimeStamp());
        generator.writeStringField(ElementName.LOG_LEVEL, eventObject.getLevel().toString());
        generator.writeStringField(ElementName.LOGGER_NAME, eventObject.getLoggerName());
        generator.writeStringField(ElementName.THREAD_NAME, eventObject.getThreadName());

        if (eventObject.hasCallerData()) {
            StackTraceElement element = eventObject.getCallerData()[eventObject.getCallerData().length - 1];
            generator.writeStringField(ElementName.CLASS_NAME, element.getClassName());
            generator.writeStringField(ElementName.METHOD_NAME, element.getMethodName());
            generator.writeNumberField(ElementName.LINE_NUMBER, element.getLineNumber());
        }

        Map<String, String> mdcPropertyMap = eventObject.getMDCPropertyMap();
        if (mdcPropertyMap != null) {
            String key;
            String value;

            for (Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                key = entry.getKey();
                value = entry.getValue();
                generator.writeStringField(key, value);
            }
        }

        List<Marker> markerList = eventObject.getMarkerList();
        if (markerList != null && !markerList.isEmpty()) {
            generator.writeArrayFieldStart(ElementName.MARKER);
            for (Marker marker : markerList) {
                generator.writeString(marker.getName());
            }
            generator.writeEndArray();
        }

        IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
        if (throwableProxy != null) {
            generator.writeFieldName("exception");
            generator.writeStartObject();
            generator.writeStringField("type", throwableProxy.getClassName());
            generator.writeStringField("message", throwableProxy.getMessage());

            generator.writeArrayFieldStart("stackTrace");
            for (StackTraceElementProxy element : throwableProxy.getStackTraceElementProxyArray()) {
                generator.writeString(element.toString());
            }
            generator.writeEndArray();

            generator.writeEndObject();
        }
        generator.writeEndObject();
    }

    @Override
    public String getFileHeader() {
        return null;
    }

    @Override
    public String getPresentationHeader() {
        return null;
    }

    @Override
    public String getPresentationFooter() {
        return null;
    }

    @Override
    public String getFileFooter() {
        return null;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void addStatus(Status status) {
        context.getStatusManager().add(status);
    }

    @Override
    public void addInfo(String msg) {
        if (context != null) {
            context.getStatusManager().add(new InfoStatus(msg, this));
        }
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        if (context != null) {
            context.getStatusManager().add(new InfoStatus(msg, this, ex));
        }
    }

    @Override
    public void addWarn(String msg) {
        if (context != null) {
            context.getStatusManager().add(new WarnStatus(msg, this));
        }
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        if (context != null) {
            context.getStatusManager().add(new WarnStatus(msg, this, ex));
        }
    }

    @Override
    public void addError(String msg) {
        if (context != null) {
            context.getStatusManager().add(new ErrorStatus(msg, this));
        }
    }

    @Override
    public void addError(String msg, Throwable ex) {
        if (context != null) {
            context.getStatusManager().add(new ErrorStatus(msg, this, ex));
        }
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
