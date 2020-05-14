/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
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
    private static final Map<String, String> EMPTY_MAP
            = Collections.unmodifiableMap(new HashMap<>());

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
