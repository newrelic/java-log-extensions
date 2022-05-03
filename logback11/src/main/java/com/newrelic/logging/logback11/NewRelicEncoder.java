/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.logback11;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.encoder.EncoderBase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * An {@link ch.qos.logback.core.encoder.Encoder} that will write New Relic's JSON format.
 *
 * To use, set this as an encoder on an appender using {@link ch.qos.logback.core.OutputStreamAppender#setEncoder(Encoder)}.
 * (This is the base class for both {@link ch.qos.logback.core.rolling.RollingFileAppender} and {@link ch.qos.logback.core.ConsoleAppender}.)
 *
 * <pre>{@code
 *     <appender name="LOG_FILE" class="ch.qos.logback.core.FileAppender">
 *         <file>logs/app-log-file.log</file>
 *         <encoder class="com.newrelic.logging.logback11.NewRelicEncoder"/>
 *     </appender>
 * }</pre>
 *
 * @see <a href="https://logback.qos.ch/manual/encoders.html#interface">Logback Encoders</a>
 */
public class NewRelicEncoder extends EncoderBase<ILoggingEvent> {
    private final NewRelicJsonLayout layout = new NewRelicJsonLayout();

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
    public byte[] encode(ILoggingEvent event) {
        String txt = layout.doLayout(event);
        return txt.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] footerBytes() {
        return new byte[0];
    }
}
