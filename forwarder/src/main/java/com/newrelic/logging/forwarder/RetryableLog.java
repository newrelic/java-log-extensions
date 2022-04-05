/*
 * Copyright 2022 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.forwarder;

import com.newrelic.telemetry.Backoff;
import com.newrelic.telemetry.logs.Log;

public class RetryableLog {
    private final Backoff backoff;
    private final Log log;

    public RetryableLog(Log givenLog) {
        log = givenLog;
        backoff = Backoff.defaultBackoff();
    }

    public Log getLog() {
        return log;
    }

    public long retryBackOffTime() {
        return backoff.nextWaitMs();
    }
}
