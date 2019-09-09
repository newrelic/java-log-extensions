/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.dropwizard;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.DropwizardLayout;
import io.dropwizard.logging.layout.DiscoverableLayoutFactory;

import java.util.TimeZone;

/**
 * The purpose of this layout factory is to provide a fallback so that
 * users only need to change the layout `type` element (between {@literal newrelic-json} and
 * {@literal log-format}) to switch between json and formatted output.
 *
 * The other option is to fully remove {@literal layout} from the YAML.
 */
@JsonTypeName("log-format")
public class LogFormatLayoutFactory implements DiscoverableLayoutFactory<ILoggingEvent> {
    private String logFormat;

    void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    @Override
    public LayoutBase<ILoggingEvent> build(LoggerContext context, TimeZone timeZone) {
        DropwizardLayout layout = new DropwizardLayout(context, timeZone);
        if(logFormat != null && !logFormat.isEmpty()) {
            layout.setPattern(logFormat);
        }
        return layout;
    }
}
