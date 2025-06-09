/*
 * Copyright 2025. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.logback13;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EncoderBase;

import java.nio.charset.StandardCharsets;

/**
 * An {@link EncoderBase} implementation that serializes {@link ILoggingEvent} instances into JSON format.
 * <p>
 * This encoder is designed to work with logback 1.3.x and uses the {@link NewRelicJsonLayout} to format the log events.
 * <p>
 * Example usage in a logback configuration file:
 * <encoder class="com.newrelic.logging.logback13.NewRelicEncoder"/>
 */
public class NewRelicEncoder extends EncoderBase<ILoggingEvent> {
    private final NewRelicJsonLayout layout = new NewRelicJsonLayout();

    @Override
    public byte[] encode(ILoggingEvent event) {
        String laidOutResult = layout.doLayout(event);
        return laidOutResult.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void start() {
        super.start();
        layout.start();
    }

    @Override
    public byte[] headerBytes() {
        return new byte[0];
    }

    @Override
    public byte[] footerBytes() {
        return new byte[0];
    }
}