/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.core;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Arrays;

import static com.newrelic.logging.core.LogExtensionConfig.getMaxStackSize;

public class ExceptionUtil {
    public static final int MAX_STACK_SIZE_DEFAULT = 300;

    public static String getFullStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        return getStackTraceStringFromFramesArray(ExceptionUtils.getStackFrames(throwable));
    }

    public static String transformLogbackStackTraceString(String trace) {
        if (trace == null || trace.length() == 0) {
            return null;
        }

        return getStackTraceStringFromFramesArray(trace.split("\n"));
    }

    private static String getStackTraceStringFromFramesArray(String[] frames) {
        int maxStackSize = getMaxStackSize();

        //We need to truncate the stacktrace based on the desired max stacktrace size, as well as remove the first line
        //of the returned trace (the "message"), since this is already captured in the error.message attribute.
        if (frames.length > maxStackSize) {
            frames = Arrays.copyOfRange(frames, 1, maxStackSize + 1);
        } else {
            frames = Arrays.copyOfRange(frames, 1, frames.length);
        }

        return String.join("\n", frames).replace("\tat", "  at") + "\n";
    }
}
