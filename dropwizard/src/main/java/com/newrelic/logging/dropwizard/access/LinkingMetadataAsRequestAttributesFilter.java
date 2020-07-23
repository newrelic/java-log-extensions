/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.dropwizard.access;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * This filter adds New Relic's linking metadata to the {@link ServletRequest} attributes.
 *
 * @see Agent#getLinkingMetadata()
 * @see ServletRequest#setAttribute(String, Object)
 */
public class LinkingMetadataAsRequestAttributesFilter implements javax.servlet.Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);

        NewRelic.getAgent().getLinkingMetadata().entrySet()
                .forEach(entry -> request.setAttribute(AccessLog.LINKING_NAMESPACE + entry.getKey(), entry.getValue()));
    }

    @Override
    public void destroy() {

    }
}
