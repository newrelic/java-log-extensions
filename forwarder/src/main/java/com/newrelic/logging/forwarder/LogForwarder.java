/*
 * Copyright 2022 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.forwarder;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.LogBatchSenderFactory;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.SenderConfiguration;
import com.newrelic.telemetry.TelemetryClient;
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.logs.LogBatch;
import com.newrelic.telemetry.logs.LogBatchSender;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A LogForwarder that will forward the logs using the NewRelic Telemetry SDK.
 *
 * This logic is in a separate class, so it can be reused by multiple appender
 * implementations across different logging libraries.
 */
public class LogForwarder {

    private static final String LICENSE_KEY_CONFIG_FIELD = "license_key";
    private static final boolean USE_DAEMON_THREADS = true;
    private static final String PLUGIN_TYPE_KEY = "plugin.type";

    private final String pluginType;
    private final BlockingQueue<Log> logs;
    private final LogForwarderConfiguration configuration;
    private final ScheduledThreadPoolExecutor executor;
    private final LogForwarderNotificationHandler notificationHandler;
    private TelemetryClient client;

    /**
     * Initialize {@link TelemetryClient} with a {@link LogBatchSender} that will forward logs to newrelic each second
     * and also manage the retry logic if the requests are failing.
     *
     * LogBatches will be sent to the TelemetryClient each second or each time the limit defined by
     * {@link LogForwarderConfiguration#getMaxLogsPerBatch()} is reached.
     *
     * LogBatches will be forwarded to NewRelic each second by the TelemetryClient.
     *
     * Logs will be dropped when {@link LogForwarderConfiguration#getMaxQueuedLogs()} is reached.
     *
     * @param givenPluginType the logging library using the forwarder.
     * @param givenConfiguration the log forwarder configuration.
     */
    public LogForwarder(String givenPluginType, LogForwarderConfiguration givenConfiguration) {
        if (agentSupplier.get() == null) throw new RuntimeException("NewRelic java-log-extensions requires the NewRelic Java Agent installed and set to work.");
        pluginType = givenPluginType;
        configuration = givenConfiguration;
        logs = new LinkedBlockingQueue<>(configuration.getMaxLogsPerBatch());
        executor = new ScheduledThreadPoolExecutor(1, Threads.daemonNamedThreadFactory("log-batcher-scheduler"));
        notificationHandler = new LogForwarderNotificationHandler(pluginType);
    }

    /**
     * Start the scheduled tasks that:
     * - Create log batches.
     * - Notify about dropped logs.
     *
     * Start the {@link TelemetryClient} that will send the logs to the NewRelic Log API.
     */
    public void start() {
        executor.scheduleAtFixedRate(this::addBatchWithCurrentLogs, configuration.getFlushIntervalSeconds(), configuration.getFlushIntervalSeconds(), TimeUnit.SECONDS);
        client = createTelemetryClient(generateSenderConfiguration());
        client.withNotificationHandler(notificationHandler);
    }

    /**
     * Schedule the log to be appended to a LogBatch.
     *
     * If the current queue size is bigger than the maxLogsPerBatch we drop the log. We use
     * the maxLogsPerBatch instead a custom configurations because we only want to limit
     * the queue somehow to prevent the log forwarder taking so much memory and affecting
     * the application.
     *
     * @param log the log to be appended.
     */
    public void append(Log log) {
        scheduleLog(new RetryableLog(log));
    }

    /**
     * Shutdowns the telemetry sdk client and the executor
     */
    public void shutdown() {
        addBatchWithCurrentLogs();
        if (client != null) {
            client.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
        if (notificationHandler != null) {
            notificationHandler.shutdown();
        }
    }

    /**
     * Generate a default configuration for the Telemetry SDK.
     *
     * @return the sender default configuration.
     */
    private SenderConfiguration generateSenderConfiguration() {
        return LogBatchSenderFactory
                .fromHttpImplementation(OkHttpPoster::new)
                .configureWith(getLicense())
                .endpoint(getEndpoint())
                .build();
    }

    /**
     * Create a telemetry client using the information from {@link LogForwarderConfiguration}.
     *
     * We only set the LogBatchSender since is the only one we're going to use.
     *
     * @param senderConfiguration the sender configuration.
     * @return the telemetry client.
     */
    protected TelemetryClient createTelemetryClient(SenderConfiguration senderConfiguration) {
        return new TelemetryClient(
                null,
                null,
                null,
                LogBatchSender.create(senderConfiguration),
                configuration.getMaxTerminationTimeSeconds(),
                USE_DAEMON_THREADS,
                configuration.getMaxQueuedLogs()
        );
    }

    /**
     * Add a log to the executor queue that will append the given log to the current batch.
     *
     * @param retryableLog a log wrapped in a RetryableLog to manage the retrying if needed.
     */
    private void scheduleLog(RetryableLog retryableLog) {
        if (executor.getQueue().size() >= configuration.getMaxScheduledLogsToBeAppended()) {
            droppedLog();
        } else {
            executor.execute(() -> appendWithRetry(retryableLog));
        }
    }

    /**
     * Schedule adding a log to the queue with the back off time provided by the RetryableLog.
     *
     * If max retries is reached or the maxQueuedLogAppend is reached, the log will be dropped.
     *
     * @param retryableLog the log to be appended.
     */
    private void scheduleLogWithDelay(RetryableLog retryableLog) {
        long waitTime = retryableLog.retryBackOffTime();
        if (waitTime == -1) {
            droppedLog();
            return;
        }

        if (executor.getQueue().size() >= configuration.getMaxScheduledLogsToBeAppended()) {
            droppedLog();
        } else {
            executor.schedule(() -> scheduleLog(retryableLog), waitTime, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * To avoid blocking the main thread this method should be executed by
     * a scheduled action.
     *
     * If the max of log per batch is raised, then we will create a batch with current
     * queued logs and send it to the telemetry SDK.
     *
     * If the logs queue refuses to add a new log, we schedule a retry..
     *
     * @param retryableLog the log to be appended.
     */
    private void appendWithRetry(RetryableLog retryableLog) {
        if (logs.size() >= configuration.getMaxLogsPerBatch()) {
            addBatchWithCurrentLogs();
        }

        if (!logs.offer(retryableLog.getLog())) {
            scheduleLogWithDelay(retryableLog);
        }
    }

    /**
     * Create a batch adding the plugin type attribute for given collection of logs.
     *
     * @param logsToBeAdded the logs to be added on the batch.
     * @return the telemetry log batch object.
     */
    private LogBatch createBatch(Collection<Log> logsToBeAdded) {
        final Attributes attributes = new Attributes();
        attributes.put(PLUGIN_TYPE_KEY, pluginType);
        return new LogBatch(logsToBeAdded, attributes);
    }

    /**
     * Drain the current logs queue and create a batch with those logs if not empty.
     *
     * Empty could occur if the application is not emitting logs and the scheduled action to send logs
     * is triggered (each getFlushIntervalSeconds).
     */
    private void addBatchWithCurrentLogs() {
        final List<Log> logsToBeAdded = new ArrayList<>();
        logs.drainTo(logsToBeAdded, configuration.getMaxLogsPerBatch());
        if (logsToBeAdded.size() > 0) {
            client.sendBatch(createBatch(logsToBeAdded));
        }
    }

    private void droppedLog() {
        notificationHandler.noticeDroppedLog();
    }

    /**
     * Get the endpoint from the configuration.
     *
     * The endpoint defaults to the us-production if the customer doesn't provide a custom one in the configuration.
     *
     * @return the logs API URL.
     */
    private URL getEndpoint() {
        try {
            return new URL(configuration.getEndpoint());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid newrelic log endpoint.", e);
        }
    }

    /**
     * Get the license form the configuration or takes the one given in the agent.
     *
     * @return the provided license or fallback to the agent one.
     */
    private String getLicense() {
        if (!configuration.getLicense().isEmpty()) {
            return configuration.getLicense();
        }
        return getAgentLicense();
    }

    private String getAgentLicense() {
        return agentSupplier.get().getConfig().getValue(LICENSE_KEY_CONFIG_FIELD);
    }

    // visible for testing
    public static Supplier<Agent> agentSupplier = NewRelic::getAgent;
}
