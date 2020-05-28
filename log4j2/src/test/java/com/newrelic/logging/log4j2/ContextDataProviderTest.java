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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.newrelic.api.agent.Agent;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verify that the ContextDataProvider is passing data.
 */
public class ContextDataProviderTest {

    @Test
    void validateTraceData() {
        Log4jLogEvent event = Log4jLogEvent.newBuilder()
                .setMessage(new ParameterizedMessage("Here's a message"))
                .setLevel(Level.ERROR)
                .setLoggerName("logger-name")
                .build();
        Map<String, String> contextMap = event.getContextData().toMap();
        assertTrue(contextMap.containsKey(NewRelicContextDataProvider.NEW_RELIC_PREFIX + "trace.id"));
    }


    @BeforeEach
    void setUp() {
        traceData = new HashMap<>();
        traceData.put("trace.id", "12345");
        Mockito.when(mockAgent.getLinkingMetadata()).thenReturn(traceData);
    }

    @AfterAll
    static void tearDownClass() {
        NewRelicContextDataProvider.agentSupplier = cachedAgent;
    }

    @BeforeAll
    static void setUpClass() {
        mockAgent = Mockito.mock(Agent.class);
        NewRelicContextDataProvider.agentSupplier = () -> mockAgent;
        cachedAgent = NewRelicContextDataProvider.agentSupplier;
    }

    private static Agent mockAgent;
    private static Supplier<Agent> cachedAgent;
    private Map<String, String> traceData;
}
