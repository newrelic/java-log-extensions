/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StackTraceTest {

    private final static String STACKTRACE_LINE_PREFIX = "at com.newrelic.logging.core.StackTraceTestMethods.stackElement";
    private final static String STACKTRACE_LINE_SUFFIX = "(StackTraceTestMethods.java:";
    private final static String CAUSED_BY_STR = "Caused by: java.lang.RuntimeException: ~~ oops ~~";

    @Test
    public void getFullStackTrace_withNoCausedByAndDefaultMaxStackSize_generatesCorrectStackTraceString() {
        Throwable testData = null;
        try {
            StackTraceTestMethods.getException();
        } catch (Throwable t) {
            testData = t;
        }

        assertNotNull(testData);
        String stackTraceStr = ExceptionUtil.getFullStackTrace(testData);
        String[] stackTraceLines = stackTraceStr.split("\n");

        assertEquals(300, stackTraceLines.length);
        for (int idx = 0; idx < 300; idx++) {
             assertTrue(stackTraceLines[idx].contains(STACKTRACE_LINE_PREFIX + (300 - idx) + STACKTRACE_LINE_SUFFIX));
        }
    }

    @Test
    public void getFullStackTrace_withCausedByAndDefaultMaxStackSize_generatesCorrectStackTraceString() {
        Throwable testData = null;
        try {
            StackTraceTestMethods.getExceptionWithCausedBy();
        } catch (Throwable t) {
            testData = t;
        }

        assertNotNull(testData);
        String stackTraceStr = ExceptionUtil.getFullStackTrace(testData);
        String[] stackTraceLines = stackTraceStr.split("\n");

        assertNotNull(stackTraceStr);
        assertTrue(stackTraceStr.contains(CAUSED_BY_STR));
        assertEquals(300, stackTraceLines.length);
    }

    @Test
    public void getFullStackTrace_withNoCausedByAndMaxStackSizeGreaterThanStackTrace_generatesCorrectStackTraceString() {
        System.setProperty(LogExtensionConfig.MAX_STACK_SIZE_SYS_PROP, "5000");
        Throwable testData = null;
        try {
            StackTraceTestMethods.getException();
        } catch (Throwable t) {
            testData = t;
        }

        assertNotNull(testData);
        String stackTraceStr = ExceptionUtil.getFullStackTrace(testData);
        String[] stackTraceLines = stackTraceStr.split("\n");

        assertTrue(stackTraceLines.length > 300);

        System.clearProperty(LogExtensionConfig.MAX_STACK_SIZE_SYS_PROP);
    }

    @Test
    public void transformLogbackStackTraceString_withEmptyString_returnsNull() {
        assertNull(ExceptionUtil.transformLogbackStackTraceString(""));
        assertNull(ExceptionUtil.transformLogbackStackTraceString(null));
    }

    @Test
    public void transformLogbackStackTraceString_withValidTrace_returnsTransformedString() {
        String traceSample = "java.lang.Exception: ~~ oops ~~\n" +
                "  at com.newrelic.logging.logback.NewRelicLogbackTests.givenALoggingEventWithExceptionData(NewRelicLogbackTests.java:143)\n" +
                "  at com.newrelic.logging.logback.NewRelicLogbackTests.shouldAppendErrorDataCorrectly(NewRelicLogbackTests.java:86)\n" +
                "  at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
                "  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)\n" +
                "  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n" +
                "  at java.lang.reflect.Method.invoke(Method.java:498)\n" +
                "Caused by: java.lang.RuntimeException: ~~ oops2! ~~\n" +
                "  at com.foo(MyMethod.java:135)\n";

        String transformedTrace = ExceptionUtil.transformLogbackStackTraceString(traceSample);
        String[] stackTraceLines = transformedTrace.split("\n");

        assertEquals(8, stackTraceLines.length);
    }

    @Test
    void shortStackTraceContents() {
        Throwable testData = new ShortStackException();
        assertNotNull(testData);
        String result = ExceptionUtil.getFullStackTrace(testData);
        assertTrue(result.contains("  at com.newrelic.logging.core.StackTraceTest.shortStackTraceContents(StackTraceTest.java:"));
    }

    static class ShortStackException extends Throwable {
        @Override
        public StackTraceElement[] getStackTrace() {
            return Arrays.copyOfRange(super.getStackTrace(), 0, 1);
        }
    }
}
