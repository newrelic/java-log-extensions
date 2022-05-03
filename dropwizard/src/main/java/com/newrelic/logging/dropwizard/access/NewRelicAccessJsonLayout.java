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
        } catch (Exception exception) {
            sw.append(exception.getMessage());
        }

        sw.append('\n');
        return sw.toString();
    }

    private void writeToGenerator(IAccessEvent event, JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        generator.writeNumberField("timestamp", event.getTimeStamp());
        generator.writeNumberField("duration", event.getElapsedTime());

        writeHttpTarget(event, generator);
        writeHttpFlavor(event, generator);

        generator.writeNumberField("http.status_code", event.getStatusCode());
        generator.writeStringField("http.method", event.getMethod());
        generator.writeStringField("http.user_agent", event.getRequestHeader("User-Agent"));
        generator.writeNumberField("http.response_content_length", event.getContentLength());
        generator.writeStringField("net.peer.ip", event.getRemoteAddr());
        generator.writeStringField("net.peer.host", event.getRemoteHost());

        writeLinkingMetadata(event, generator);

        generator.writeEndObject();
    }

    private void writeLinkingMetadata(IAccessEvent event, JsonGenerator generator) throws IOException {
        for (String key : Arrays.asList("entity.guid", "entity.name", "entity.type", "hostname", "trace.id")) {
            String value = event.getAttribute(AccessLog.LINKING_NAMESPACE + key);
            if (value != null && !value.equals("") && !value.equals("-")) {
                generator.writeStringField(key, value);
            }
        }
    }

    private void writeHttpFlavor(IAccessEvent event, JsonGenerator generator) throws IOException {
        String protocol = event.getProtocol();
        if (protocol != null) {
            String[] protocolPieces = protocol.split("/");
            if (protocolPieces.length > 1) {
                generator.writeStringField("http.flavor", protocolPieces[1]);
            }
        }
    }

    private void writeHttpTarget(IAccessEvent event, JsonGenerator generator) throws IOException {
        String target = "";
        if (event.getRequestURI() != null && !event.getRequestURI().equals("")) {
            target += event.getRequestURI();
        }
        if (event.getQueryString() != null && !event.getQueryString().equals("")) {
            target += event.getQueryString();
        }
        generator.writeStringField("http.target", target);
    }
}
