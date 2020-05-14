/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.log4j2;

import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * This class is provided for backward compatibility and is now a no-op.
 *
 * @deprecated Use NewRelicContextProvider instead.
 */
public class NewRelicMessage extends ParameterizedMessage {

    public NewRelicMessage(String messagePattern, Object[] arguments, Throwable throwable) {
        super(messagePattern, arguments, throwable);
    }

    public NewRelicMessage(String messagePattern, Object... arguments) {
        super(messagePattern, arguments);
    }
}
