/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import static com.newrelic.logging.core.ElementName.TIMESTAMP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class LogAsserts {

    public static void assertFieldValues(String result, Map<String, Object> expectedValues) throws IOException {
        assertEquals('\n', result.charAt(result.length() - 1));

        JsonParser parser = new JsonFactory().createParser(result);
        assertEquals(parser.nextToken(), JsonToken.START_OBJECT);

        JsonToken token;
        while ((token = parser.nextToken()) != JsonToken.END_OBJECT) {
            assertEquals(token, JsonToken.FIELD_NAME);
            String elementName = parser.getValueAsString();
            assertValueIfPresent(elementName, parser, expectedValues);
        }
    }

    private static void assertValueIfPresent(String elementName, JsonParser parser, Map<String, Object> expectedValues) throws IOException {
        JsonToken valueToken = parser.nextToken();
        if (elementName.equals(TIMESTAMP)) {
            /* Check timestamp for long regardless of knowing its expected value */
            assertEquals(valueToken, JsonToken.VALUE_NUMBER_INT);
        }

        if (expectedValues.containsKey(elementName)) {
            Object expectedValue = expectedValues.get(elementName);
            if (expectedValue == null) {
                assertEquals(valueToken, JsonToken.VALUE_NULL);
                return;
            }

            switch (expectedValue.getClass().getName()) {
                case "java.lang.Long":
                    assertEquals(valueToken, JsonToken.VALUE_NUMBER_INT);
                    assertEquals(parser.getValueAsLong(), expectedValue);
                    break;
                case "java.lang.String":
                    assertEquals(valueToken, JsonToken.VALUE_STRING);
                    assertEquals(parser.getValueAsString(), expectedValue);
                    break;
                case "java.util.regex.Pattern":
                    Pattern pattern = (Pattern)expectedValue;
                    assertEquals(valueToken, JsonToken.VALUE_STRING);
                    assertTrue(
                            pattern.matcher(parser.getValueAsString()).matches(),
                            "Regex >>" + pattern.pattern() + "<< not present in string: >>" + parser.getValueAsString() + "<<");
                    break;
                default:
                    fail("Unexpected value type, add a case here to handle it: " + expectedValue.getClass());
            }
        }
    }
}
