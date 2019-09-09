/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.log4j1;

import org.apache.log4j.AsyncAppender;
import org.apache.log4j.spi.LoggingEvent;

public class NewRelicAsyncAppender extends AsyncAppender {
    @Override
    public void append(LoggingEvent event) {
        if (event instanceof NewRelicLoggingEvent) {
            super.append(event);
        } else {
            super.append(new NewRelicLoggingEvent(event));
        }
    }
}
