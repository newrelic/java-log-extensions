# Log Forwarder

A log forwarder built using the [New Relic Java Telemetry SDK](https://github.com/newrelic/newrelic-telemetry-sdk-java).

## Usage 

The `forwarder` module is currently used by the following:
* [Logback 1.2](../logback/README.md) - See `NewRelicHttpAppender`

## Configuration

The `forwarder` provides the following configuration options via `LogForwarderConfiguration`:
* `endpoint`: The `endpoint` defaults to New Relic US production environments (https://log-api.newrelic.com/log/v1) and will need to be configured for other environments (e.g. EU production should instead
  use https://log-api.eu.newrelic.com/log/v1).
* `license`: The `license` will be picked up from the java-agent if installed, but you can override it if you want.
* `maxQueuedLogs`: Maximum number of logs queued in memory waiting to be sent.
* `maxLogsPerBatch`: Maximum number of logs per batch (request) to NewRelic.
* `maxTerminationTimeSeconds`: Number of seconds to wait for graceful shutdown of its executor.
* `flushIntervalSeconds`: Time period and initial delay when scheduling a task at a fixed rate.
* `maxScheduledLogsToBeAppended`: Maximum scheduled logs to be appended. This is used to prevent the log forwarder from accepting more logs when we reach this number of jobs in the scheduler.
