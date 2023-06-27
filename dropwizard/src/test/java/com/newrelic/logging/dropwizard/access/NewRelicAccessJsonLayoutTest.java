/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.dropwizard.access;

import ch.qos.logback.access.spi.IAccessEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NewRelicAccessJsonLayoutTest {
    @Test
    void generatesDecentData() {
        givenMockDataForBasicFields();
        whenMockEventIsSerialized();
        thenStartsAndEndsWithCurlyBraces();
        thenBasicFieldsAreSerialized();
    }

    @Test
    void isNullSafe() {
        // no mocking, all stubs return null
        whenMockEventIsSerialized();
        // the formatter will only output closing curly brace if there are no exceptions.
        thenStartsAndEndsWithCurlyBraces();
    }

    @Test
    void doesNotSetFlavorWithoutAtLeastOneSlash() {
        givenProtocolHasNoSlashes();
        whenMockEventIsSerialized();
        thenStartsAndEndsWithCurlyBraces();
        thenDoesNotContainHttpFlavor();
    }

    @Test
    void omitsNullEmptyAndDashLinkingMetadata() {
        givenLinkingMetadataWithVariations();
        whenMockEventIsSerialized();
        thenStartsAndEndsWithCurlyBraces();
        thenNullEmptyAndDashFieldsAreNotIncluded();
        thenPopulatedFieldsAreIncluded();
    }

    @ParameterizedTest
    @MethodSource("threeStringHttpTarget")
    void shouldGenerateTargetStringSafely(String uri, String query, String expected) {
        givenMockDataForRequestURIAndQuery(uri, query);
        whenMockEventIsSerialized();
        thenHttpTargetShouldBeSafe(expected);
    }

    static Stream<Arguments> threeStringHttpTarget() {
        return Stream.of(
                arguments("r", "q", "rq"),
                arguments("r", null, "r"),
                arguments("r", "", "r"),
                arguments(null, "q", "q"),
                arguments("", "q", "q"),
                arguments("", "", ""),
                arguments(null, "", ""),
                arguments("", null, ""),
                arguments(null, null, "")
        );
    }

    private void givenMockDataForRequestURIAndQuery(String uri, String query) {
        when(mockEvent.getRequestURI()).thenReturn(uri);
        when(mockEvent.getQueryString()).thenReturn(query);
    }

    private void givenLinkingMetadataWithVariations() {
        when(mockEvent.getAttribute("com.newrelic.linking.entity.guid")).thenReturn(null);
        when(mockEvent.getAttribute("com.newrelic.linking.entity.name")).thenReturn("");
        when(mockEvent.getAttribute("com.newrelic.linking.entity.type")).thenReturn("-");
        when(mockEvent.getAttribute("com.newrelic.linking.trace.id")).thenReturn("some-trace");
    }

    private void givenProtocolHasNoSlashes() {
        when(mockEvent.getProtocol()).thenReturn("http");
    }

    private void givenMockDataForBasicFields() {
        when(mockEvent.getTimeStamp()).thenReturn(12345L);
        when(mockEvent.getElapsedTime()).thenReturn(234L);
        when(mockEvent.getProtocol()).thenReturn("HTTP/1.1");
        when(mockEvent.getRequestURI()).thenReturn("/some/path");
        when(mockEvent.getQueryString()).thenReturn("?param=value");
        when(mockEvent.getStatusCode()).thenReturn(202);
        when(mockEvent.getMethod()).thenReturn("GET");
        when(mockEvent.getRequestHeader("User-Agent")).thenReturn("curl/7.64.31");
        when(mockEvent.getContentLength()).thenReturn(3234L);
        when(mockEvent.getRemoteAddr()).thenReturn("remote-addr");
        when(mockEvent.getRemoteHost()).thenReturn("remote-host");
    }

    private void whenMockEventIsSerialized() {
        NewRelicAccessJsonLayout target = new NewRelicAccessJsonLayout();
        result = target.doLayout(mockEvent);
    }

    private void thenStartsAndEndsWithCurlyBraces() {
        assertThat(result, startsWith("{"));
        assertThat(result.trim(), endsWith("}"));
    }

    private void thenBasicFieldsAreSerialized() {
        assertThat(result, containsString("\"timestamp\":12345"));
        assertThat(result, containsString("\"duration\":234"));
        assertThat(result, containsString("\"http.flavor\":\"1.1\""));
        assertThat(result, containsString("\"http.target\":\"/some/path?param=value\""));
        assertThat(result, containsString("\"http.status_code\":202"));
        assertThat(result, containsString("\"http.user_agent\":\"curl/7.64.31\""));
        assertThat(result, containsString("\"http.response_content_length\":3234"));
        assertThat(result, containsString("\"net.peer.ip\":\"remote-addr\""));
        assertThat(result, containsString("\"net.peer.host\":\"remote-host\""));
    }

    private void thenDoesNotContainHttpFlavor() {
        assertThat(result, not(containsString("http.flavor")));
    }

    private void thenNullEmptyAndDashFieldsAreNotIncluded() {
        assertThat(result, not(containsString("entity.guid")));
        assertThat(result, not(containsString("entity.type")));
        assertThat(result, not(containsString("entity.name")));
    }

    private void thenPopulatedFieldsAreIncluded() {
        assertThat(result, containsString("\"trace.id\":\"some-trace\""));
    }

    private void thenHttpTargetShouldBeSafe(String expected) {
        assertThat(result, containsString("\"http.target\":\"" + expected + "\""));
    }

    @Mock
    IAccessEvent mockEvent;

    private String result;
}