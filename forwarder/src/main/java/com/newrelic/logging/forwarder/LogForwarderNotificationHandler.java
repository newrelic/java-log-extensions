package com.newrelic.logging.forwarder;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.telemetry.NotificationHandler;
import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link NotificationHandler} that handle errors and information messages generated
 * by the telemetry SDK and forward them to NewRelic.
 *
 * Telemetry SDK default handler is logging based, and it doesn't help use in case
 * we can't forward the logs to NewRelic because an error.
 *
 * It relies on the {@link NewRelic} {@link com.newrelic.api.agent.Agent} since is a
 * common dependency for all the logging extensions contained on this project.
 *
 * If the Telemetry SDK notify about a retryable message, this notification handler
 * informs NewRelic generating an event on NrIntegrationError with the message. This
 * will be useful when customer want to tune-up their log forwarder (in case that logs
 * are being split, or they're hitting the API rate-limiting)
 *
 * If any error is noticed from the Telemetry SDK we sent it directly to NewRelic as
 * an error (APM) but also register the amount of dropped logs produced by it.
 *
 * This notification handler inform NewRelic about dropped logs in two ways, one is
 * adding a NrIntegrationError every 30 seconds with the amount of dropped logs. The
 * other one is sending the metric `droppedLogs` to a custom event named
 * "LogForwarderMonitoring".
 */
public class LogForwarderNotificationHandler implements NotificationHandler {

    private final String pluginType;
    private final AtomicInteger droppedLogs = new AtomicInteger();
    private final ScheduledThreadPoolExecutor executor;

    public LogForwarderNotificationHandler(String givenPluginType) {
        pluginType = givenPluginType;
        executor = new ScheduledThreadPoolExecutor(1, Threads.daemonNamedThreadFactory("log-notification-scheduler"));
        executor.scheduleAtFixedRate(this::notifyDroppedLogs, 30, 30, TimeUnit.SECONDS);
    }

    @Override
    public void noticeError(String message, Throwable throwable, TelemetryBatch<? extends Telemetry> batch) {
        incrementDroppedLogsCounter(batch.size());
        Map<String, Object> attributes = agentAttributes();
        attributes.put("batch.size", batch.size());
        NewRelic.noticeError(throwable, attributes);
    }

    @Override
    public void noticeInfo(String message, Exception exception, TelemetryBatch<? extends Telemetry> batch) {
        Map<String, Object> attributes = agentAttributes();
        attributes.put("batch.size", batch.size());
        attributes.put("message", message);
        if (exception != null) {
            attributes.put("exception.message", exception.getMessage());
            attributes.put("exception.stacktrace", exception.getStackTrace());
        }
        NewRelic.getAgent().getInsights().recordCustomEvent("NrIntegrationError", attributes);
    }

    /**
     * Notice that a log line has been dropped.
     */
    public void noticeDroppedLog() {
        incrementDroppedLogsCounter(1);
    }

    /**
     * Shutdown the executor.
     */
    public void shutdown() {
        executor.shutdown();
    }

    private void incrementDroppedLogsCounter(int count) {
        droppedLogs.addAndGet(count);
        NewRelic.incrementCounter(String.format("Custom/LogForwarderMonitoring/%s/droppedLogs", pluginType), count);
    }

    private Map<String, Object> agentAttributes() {
        return new HashMap<>(NewRelic.getAgent().getLinkingMetadata());
    }

    private void notifyDroppedLogs() {
        if (droppedLogs.get() > 0) {
            int currentDroppedLogs = droppedLogs.getAndSet(0);
            Map<String, Object> attributes = agentAttributes();
            attributes.put("message", "Dropped " + currentDroppedLogs + " logs.");
            NewRelic.getAgent().getInsights().recordCustomEvent("NrIntegrationError", attributes);
        }
    }
}
