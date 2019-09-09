/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.log4j2;

import com.newrelic.api.agent.Agent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageTest {
    @Test
    void shouldGatherTraceDataOnConstruction() {
        assertEquals(traceData, new NewRelicMessage("pattern").getTraceData());
        assertEquals(traceData, new NewRelicMessage("pattern", 42).getTraceData());
        assertEquals(traceData, new NewRelicMessage("pattern", new Object[] {}, new Exception()).getTraceData());
    }

    @BeforeEach
    void setUp() {
        traceData = new HashMap<>();
        Mockito.when(mockAgent.getLinkingMetadata()).thenReturn(traceData);
    }

    @AfterAll
    static void tearDownClass() {
        NewRelicMessage.agentSupplier = cachedAgent;
    }

    @BeforeAll
    static void setUpClass() {
        mockAgent = Mockito.mock(Agent.class);
        NewRelicMessage.agentSupplier = () -> mockAgent;
        cachedAgent = NewRelicMessage.agentSupplier;
    }

    private static Agent mockAgent;
    private static Supplier<Agent> cachedAgent;
    private Map<String, String> traceData;
}
