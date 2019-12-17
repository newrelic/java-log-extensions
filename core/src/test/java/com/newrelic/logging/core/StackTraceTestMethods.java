/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.core;

public class StackTraceTestMethods {
    public static void getException() {
        stackElement0();
    }

    private static void stackElement0() {
        stackElement1();
    }

    private static void stackElement1() {
        stackElement2();
    }

    private static void stackElement2() {
        stackElement3();
    }

    private static void stackElement3() {
        stackElement4();
    }

    private static void stackElement4() {
        stackElement5();
    }

    private static void stackElement5() {
        stackElement6();
    }

    private static void stackElement6() {
        stackElement7();
    }

    public static void stackElement7() {
        stackElement8();
    }

    private static void stackElement8() {
        stackElement9();
    }

    private static void stackElement9() {
        stackElement10();
    }

    private static void stackElement10() {
        throw new RuntimeException("~~ oops ~~");
    }
}
