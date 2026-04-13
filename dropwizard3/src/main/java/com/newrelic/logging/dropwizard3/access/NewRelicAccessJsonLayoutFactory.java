/*
 * Copyright 2026 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.dropwizard3.access;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.common.layout.DiscoverableLayoutFactory;

import java.util.TimeZone;

@JsonTypeName("newrelic-access-json")
public class NewRelicAccessJsonLayoutFactory implements DiscoverableLayoutFactory<IAccessEvent> {
    @Override
    public LayoutBase<IAccessEvent> build(LoggerContext context, TimeZone timeZone) {
        return new NewRelicAccessJsonLayout();
    }
}
