/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.dropwizard.access;

import com.google.common.collect.ImmutableMap;
import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LinkingMetadataAsRequestAttributesFilterTest {

    private Supplier<Agent> agentSupplier = NewRelic::getAgent;

    @Test
    void addsAndPrefixesLinkingMetadata() throws IOException, ServletException {
        givenMockAgentData();
        whenTheFilterIsInvoked();
        thenTheChainContinued();
        thenTheAttributesWereSet();
    }

    @Test
    void worksWithNoAgentData() throws IOException, ServletException {
        whenTheFilterIsInvoked();
        thenTheChainContinued();
        thenNoAttributesWereSet();
    }

    private void givenMockAgentData() {
        Agent mockAgent = mock(Agent.class);
        Mockito.when(mockAgent.getLinkingMetadata()).thenReturn(ImmutableMap.of("some.key", "some.value", "other.key", "other.value"));
        agentSupplier = () -> mockAgent;
    }

    private void whenTheFilterIsInvoked() throws IOException, ServletException {
        LinkingMetadataAsRequestAttributesFilter target = new LinkingMetadataAsRequestAttributesFilter(agentSupplier);
        target.doFilter(mockRequest, mock(ServletResponse.class), mockChain);
    }

    private void thenTheAttributesWereSet() {
        verify(mockRequest, times(1)).setAttribute("com.newrelic.linking.some.key", "some.value");
        verify(mockRequest, times(1)).setAttribute("com.newrelic.linking.other.key", "other.value");
    }

    private void thenNoAttributesWereSet() {
        verify(mockRequest, never()).setAttribute(anyString(), anyString());
    }

    private void thenTheChainContinued() throws IOException, ServletException {
        verify(mockChain, times(1)).doFilter(eq(mockRequest), any(ServletResponse.class));
    }

    @Mock
    ServletRequest mockRequest;

    @Mock
    FilterChain mockChain;
}