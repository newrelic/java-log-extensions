/*
 * Copyright 2022 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.forwarder;

public class LogForwarderConfiguration {

    public static final String DEFAULT_URL = "https://log-api.newrelic.com/log/v1";
    public static final String DEFAULT_LICENSE = "";
    public static final int DEFAULT_MAX_LOGS_PER_BATCH = 10_000;
    public static final int DEFAULT_MAX_QUEUED_LOGS = 100_000;
    public static final int DEFAULT_MAX_SCHEDULED_LOGS_TO_BE_APPENDED = 1_000;
    public static final int DEFAULT_MAX_TERMINATION_TIME_SECONDS = 10;
    public static final int DEFAULT_FLUSH_INTERVAL_SECONDS = 1;

    private final String endpoint;
    private final String license;

    /**
     * Maximum number of logs per batch (request) to NewRelic.
     */
    private final int maxLogsPerBatch;

    /**
     * Maximum number of logs queued in memory waiting to be sent.
     */
    private final int maxQueuedLogs;

    /**
     * Maximum scheduled logs to be appended.
     *
     * This is used to prevent the log forwarder from accepting more logs when we reach this
     * number of jobs in the scheduler.
     */
    private final int maxScheduledLogsToBeAppended;

    /**
     * Time period and initial delay when scheduling a task at a fixed rate.
     */
    private final int flushIntervalSeconds;

    /**
     * Number of seconds to wait for graceful shutdown of its executor.
     */
    private final int maxTerminationTimeSeconds;

    private LogForwarderConfiguration(Builder builder) {
        endpoint = builder.endpoint;
        license = builder.license;
        maxLogsPerBatch = builder.maxLogsPerBatch;
        maxQueuedLogs = builder.maxQueuedLogs;
        maxTerminationTimeSeconds = builder.maxTerminationTimeSeconds;
        flushIntervalSeconds = builder.flushIntervalSeconds;
        maxScheduledLogsToBeAppended = builder.maxScheduledLogsToBeAppended;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getLicense() {
        return license;
    }

    public int getMaxQueuedLogs() {
        return maxQueuedLogs;
    }

    public int getMaxLogsPerBatch() {
        return maxLogsPerBatch;
    }

    public int getMaxTerminationTimeSeconds() {
        return maxTerminationTimeSeconds;
    }

    public int getFlushIntervalSeconds() {
        return flushIntervalSeconds;
    }

    public int getMaxScheduledLogsToBeAppended() {
        return maxScheduledLogsToBeAppended;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String endpoint = DEFAULT_URL;
        private String license = DEFAULT_LICENSE;
        private int maxQueuedLogs = DEFAULT_MAX_QUEUED_LOGS;
        private int maxLogsPerBatch = DEFAULT_MAX_LOGS_PER_BATCH;
        private int maxTerminationTimeSeconds = DEFAULT_MAX_TERMINATION_TIME_SECONDS;
        private int flushIntervalSeconds = DEFAULT_FLUSH_INTERVAL_SECONDS;
        private int maxScheduledLogsToBeAppended = DEFAULT_MAX_SCHEDULED_LOGS_TO_BE_APPENDED;

        private Builder() {}

        public Builder setEndpoint(String givenEndpoint) {
            endpoint = givenEndpoint;
            return this;
        }

        public Builder setLicense(String givenLicense) {
            license = givenLicense;
            return this;
        }

        public Builder setMaxQueuedLogs(int givenMaxQueuedLogs) {
            maxQueuedLogs = givenMaxQueuedLogs;
            return this;
        }

        public Builder setMaxLogsPerBatch(int givenMaxLogsPerBatch) {
            maxLogsPerBatch = givenMaxLogsPerBatch;
            return this;
        }

        public Builder setMaxTerminationTimeSeconds(int givenMaxTerminationTimeSeconds) {
            maxTerminationTimeSeconds = givenMaxTerminationTimeSeconds;
            return this;
        }

        public Builder setFlushIntervalSeconds(int givenFlushIntervalSeconds) {
            flushIntervalSeconds = givenFlushIntervalSeconds;
            return this;
        }

        public Builder setMaxScheduledLogsToBeAppended(int givenMaxScheduledLogsToBeAppended) {
            maxScheduledLogsToBeAppended = givenMaxScheduledLogsToBeAppended;
            return this;
        }

        public LogForwarderConfiguration build() {
            return new LogForwarderConfiguration(this);
        }
    }

}
