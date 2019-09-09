/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.dropwizard;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.newrelic.logging.logback.NewRelicAsyncAppender;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;

@JsonTypeName("newrelic")
public class NewRelicAsyncAppenderFactory extends AsyncLoggingEventAppenderFactory {
    @Override
    public AsyncAppenderBase<ILoggingEvent> build() {
        return new NewRelicAsyncAppender();
    }
}
