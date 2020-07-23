/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.dropwizard.access;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Provides a representation of the {@link IAccessEvent} in JSON. The attribute names are sourced from
 * <a href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/semantic_conventions/http.md">OpenTelemetry</a>.
 */
public class NewRelicAccessJsonLayout extends LayoutBase<IAccessEvent> {
    @Override
    public String doLayout(IAccessEvent event) {
        StringWriter sw = new StringWriter();

        try (JsonGenerator generator = new JsonFactory().createGenerator(sw)) {
            writeToGenerator(event, generator);
        } catch (Throwable ignored) {
            return event.getRequestURI();
        }

        sw.append('\n');
        return sw.toString();
    }

    private void writeToGenerator(IAccessEvent event, JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeNumberField("timestamp", event.getTimeStamp());
        generator.writeNumberField("duration", event.getElapsedTime());

        String target = event.getRequestURI();
        if (event.getQueryString() != null && !event.getQueryString().equals("")) {
            target += event.getQueryString();
        }
        generator.writeStringField("http.target", target);

        String[] protocolPieces = event.getProtocol().split("/");
        if (protocolPieces.length > 1) {
            generator.writeStringField("http.flavor", protocolPieces[1]);
        }

        generator.writeNumberField("http.status_code", event.getStatusCode());
        generator.writeStringField("http.method", event.getMethod());
        generator.writeStringField("http.user_agent", event.getRequestHeader("User-Agent"));
        generator.writeNumberField("http.response_content_length", event.getContentLength());
        generator.writeStringField("net.peer.ip", event.getRemoteAddr());
        generator.writeStringField("net.peer.host", event.getRemoteHost());

        for (String key : Arrays.asList("entity.guid", "entity.name", "entity.type", "hostname", "trace.id")) {
            String value = event.getAttribute(AccessLog.LINKING_NAMESPACE + key);
            if (!Objects.equals(value, "")) {
                generator.writeStringField(key, value);
            }
        }

        generator.writeEndObject();
    }
}
