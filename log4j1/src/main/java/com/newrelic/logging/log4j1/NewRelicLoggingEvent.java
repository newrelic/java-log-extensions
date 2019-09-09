/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.log4j1;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Map;
import java.util.function.Supplier;

class NewRelicLoggingEvent extends LoggingEvent {
    NewRelicLoggingEvent(LoggingEvent event) {
        super(event.fqnOfCategoryClass, event.getLogger(), event.timeStamp, event.getLevel(), event.getMessage(), event.getThreadName(),
                event.getThrowableInformation(), event.getNDC(), event.getLocationInformation(), event.getProperties());
        linkedMetadata = agentSupplier.get().getLinkingMetadata();
    }

    private final Map<String, String> linkedMetadata;

    Map<String, String> getLinkedMetadata() {
        return linkedMetadata;
    }

    @SuppressWarnings("WeakerAccess") // visible for testing
    static Supplier<Agent> agentSupplier = NewRelic::getAgent;
}
