/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.log4j2;

import org.apache.logging.log4j.message.AbstractMessageFactory;
import org.apache.logging.log4j.message.Message;

/**
 * A {@link org.apache.logging.log4j.message.MessageFactory2} that constructs {@link NewRelicMessage} instances.
 *
 * This class is required to capture New Relic trace information at the time of the log message. To use this,
 * set a system property. The precise method will vary on your application framework, but the most basic process is to
 * add `-Dproperty=value` to your `java` command-line.
 *
 * <pre>{@code
 * log4j2.messageFactory=com.newrelic.logging.log4j2.NewRelicMessageFactory
 * }</pre>
 *
 * @see <a href="https://logging.apache.org/log4j/2.x/manual/configuration.html#SystemProperties">Log4j 2.x System Properties</a>
 */
public class NewRelicMessageFactory extends AbstractMessageFactory {
    @Override
    public Message newMessage(String message, Object... params) {
        return new NewRelicMessage(message, params);
    }

    @Override
    public Message newMessage(final CharSequence message) {
        return newMessage(message.toString());
    }

    @Override
    public Message newMessage(final Object message) {
        return new NewRelicMessage(String.valueOf(message));
    }

    @Override
    public Message newMessage(final String message) {
        return new NewRelicMessage(message);
    }

}
