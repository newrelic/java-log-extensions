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
    private NewRelicJsonLayout layout = new NewRelicJsonLayout();

    @Override
    public void doEncode(ILoggingEvent event) throws IOException {
        String laidOutResult = layout.doLayout(event);
        ByteBuffer results = StandardCharsets.UTF_8.encode(laidOutResult);
        byte[] resultArray = results.array();
        int i = resultArray.length - 1;
        while (i > 0 && resultArray[i - 1] == '\0') {
            i--;
        }

        try {
            super.outputStream.write(Arrays.copyOfRange(resultArray, 0, i));
            super.outputStream.flush();
        } catch (IOException e) {
            addWarn("Error encountered while writing log event. Event: " + event, e);
        }
    }

    @Override
    public void start() {
        super.start();
        layout.start();
    }

    @Override
    public void close() throws IOException {
    }
}
