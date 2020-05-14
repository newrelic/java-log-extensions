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
 * @deprecated NewRelicMessages are no longer required and has been superceded by NewRelicContextDataProvider.
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
