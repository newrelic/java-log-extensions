/*
 * Copyright 2025. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback13;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

import java.util.List;
import java.util.Map;

public class CustomLoggingEventWrapper implements ILoggingEvent {
    private final ILoggingEvent delegate;
    private final Map<String, String> customMdc;

    public CustomLoggingEventWrapper(ILoggingEvent delegate, Map<String, String> mdcOverride) {
        this.delegate = delegate;
        this.customMdc = mdcOverride;
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        return customMdc;
    }

    @Override
    public Map<String, String> getMdc() {
        return customMdc;
    }

    @Override
    public String getThreadName() {
        return delegate.getThreadName();
    }

    @Override
    public Level getLevel() {
        return delegate.getLevel();
    }

    @Override
    public String getMessage() {
        return delegate.getMessage();
    }

    @Override
    public Object[] getArgumentArray() {
        return delegate.getArgumentArray();
    }

    @Override
    public String getFormattedMessage() {
        return delegate.getFormattedMessage();
    }

    @Override
    public String getLoggerName() {
        return delegate.getLoggerName();
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return delegate.getLoggerContextVO();
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return delegate.getThrowableProxy();
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return delegate.getCallerData();
    }

    @Override
    public boolean hasCallerData() {
        return delegate.hasCallerData();
    }

    @Override
    public List<Marker> getMarkerList() {
        return delegate.getMarkerList();
    }

    @Override
    public long getTimeStamp() {
        return delegate.getTimeStamp();
    }

    @Override
    public int getNanoseconds() {
        return delegate.getNanoseconds();
    }

    @Override
    public long getSequenceNumber() {
        return delegate.getSequenceNumber();
    }

    @Override
    public List<KeyValuePair> getKeyValuePairs() {
        return delegate.getKeyValuePairs();
    }

    @Override
    public void prepareForDeferredProcessing() {
        delegate.prepareForDeferredProcessing();
    }
}
