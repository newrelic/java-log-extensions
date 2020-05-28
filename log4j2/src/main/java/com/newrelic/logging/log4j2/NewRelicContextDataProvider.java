/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.log4j2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.logging.log4j.core.util.ContextDataProvider;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;

/**
 * Injects the trace data into the log event.
 */
public class NewRelicContextDataProvider implements ContextDataProvider {
    //visible for testing
    static Supplier<Agent> agentSupplier = NewRelic::getAgent;
    public static final String NEW_RELIC_PREFIX = "NewRelic:";
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    @Override
    public Map<String, String> supplyContextData() {
        Map<String, String> meta = agentSupplier.get().getLinkingMetadata();
        if (meta != null && meta.size() > 0) {
            Map<String, String> map = new HashMap<>();
            Set<Map.Entry<String, String>> metaSet = meta.entrySet();
            for (Map.Entry<String, String> entry : metaSet) {
                map.put(NEW_RELIC_PREFIX + entry.getKey(), entry.getValue());
            }
            return map;
        } else {
            return EMPTY_MAP;
        }
    }
}
