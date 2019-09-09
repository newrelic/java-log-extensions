/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.dropwizard;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.newrelic.logging.logback.NewRelicJsonLayout;
import io.dropwizard.logging.layout.DiscoverableLayoutFactory;

import java.util.TimeZone;

@JsonTypeName("newrelic-json")
public class NewRelicJsonLayoutFactory implements DiscoverableLayoutFactory<ILoggingEvent> {
    @Override
    public LayoutBase<ILoggingEvent> build(LoggerContext context, TimeZone timeZone) {
        return new NewRelicJsonLayout();
    }
}
