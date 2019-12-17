/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StackTraceTest {
    @Test
    void stackTraceContents() {
        Throwable testData = null;
        try {
            StackTraceTestMethods.getException();
        } catch (Throwable t) {
            testData = t;
        }
        assertNotNull(testData);
        String result = ExceptionUtil.getErrorStack(testData);
        assertEquals("  at com.newrelic.logging.core.StackTraceTestMethods.stackElement10(StackTraceTestMethods.java:54)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement9(StackTraceTestMethods.java:50)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement8(StackTraceTestMethods.java:46)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement7(StackTraceTestMethods.java:42)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement6(StackTraceTestMethods.java:38)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement5(StackTraceTestMethods.java:34)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement4(StackTraceTestMethods.java:30)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement3(StackTraceTestMethods.java:26)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement2(StackTraceTestMethods.java:22)\n"
                        + "  at com.newrelic.logging.core.StackTraceTestMethods.stackElement1(StackTraceTestMethods.java:18)\n",
                result);
    }

    @Test
    void shortStackTraceContents() {
        Throwable testData = new ShortStackException();
        assertNotNull(testData);
        String result = ExceptionUtil.getErrorStack(testData);
        assertEquals("  at com.newrelic.logging.core.StackTraceTest.shortStackTraceContents(StackTraceTest.java:41)\n",
                result);
    }

    static class ShortStackException extends Throwable {
        @Override
        public StackTraceElement[] getStackTrace() {
            return Arrays.copyOfRange(super.getStackTrace(), 0, 1);
        }
    }
}
