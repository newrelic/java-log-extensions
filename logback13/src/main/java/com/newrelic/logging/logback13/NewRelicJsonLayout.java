/*
 * Copyright 2025. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback13;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.WarnStatus;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ElementName;
import com.newrelic.logging.core.LogExtensionConfig;
import com.fasterxml.jackson.core.JsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.NOPMDCAdapter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.newrelic.logging.core.LogExtensionConfig.CONTEXT_PREFIX;
import static com.newrelic.logging.logback13.NewRelicAsyncAppender.NEW_RELIC_PREFIX;

/**
 * A layout that formats log events as JSON objects, suitable for use with New Relic.
 * Adds standard fields such and injects linking metadata using prefixed MDC keys.
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
        } catch (IOException ignored) {
            return eventObject.getFormattedMessage();
        }

        return sw.toString();
    }

    private void writeToGenerator(ILoggingEvent eventObject, JsonGenerator generator) throws IOException {
        boolean isNoOpMDC = MDC.getMDCAdapter() instanceof NOPMDCAdapter;

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
            for (Map.Entry<String, String> entry : mdcPropertyMap.entrySet()) {
                generator.writeStringField(entry.getKey(), entry.getValue());
            }
        } else if (!isNoOpMDC) {
            for (Map.Entry<String, String> entry : MDC.getCopyOfContextMap().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null || value.isEmpty()) {
                    continue;
                }
                if (key.startsWith(NEW_RELIC_PREFIX)) {
                    generator.writeStringField(key.substring(NEW_RELIC_PREFIX.length()), value);
                } else {
                    generator.writeStringField(CONTEXT_PREFIX + key, value);
                }
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
