# The New Relic Logback Extension

## Preconditions

1. logback 1.2.x must be configured and working in the application.
2. The New Relic Java agent must be enabled using the `-javaagent` command-line parameter.
3. You must be using at least version 5.6.0 of the Java Agent.

## Configuring

There are some changes to your application to use the New Relic
Logback Extension. All steps are required.

### 1. Include the dependency in your project.

Refer to [Maven Central](https://search.maven.org/search?q=g:com.newrelic.logging%20a:logback) for the appropriate snippets.

### 2. Configure a `logback appender` element with a `NewRelicEncoder` or a `NewRelicHttpAppender` if you want to send

#### Using an external log forwarder like the infra-agent (fluent-bit), fluentd or others.

Update your logging configuration xml to include the `<encoder>` element like below.

```xml
    <appender name="LOG_FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/app-log-file.log</file>
        <encoder class="com.newrelic.logging.logback.NewRelicEncoder"/>
    </appender>
```

*Why?* The New Relic log format is a tailored JSON format with specific fields in specific places
that our log forwarder plugins and back end rely on. At this time, we don't support any customization
of that format.

#### Using the http log forwarder from within your app.

We provide an HTTP log forwarder within the logback extension that will fit for some use cases where install
external log forwarders is not an option. For that you should configure the `NewRelicHttpAppender` as follows.

```xml
    <appender name="HTTP" class="com.newrelic.logging.logback.NewRelicHttpAppender">
        <!-- Those are the default configuration values -->
        <!-- <endpoint>https://log-api.newrelic.com/log/v1</endpoint> -->
        <!-- <license></license> -->
        <!-- <maxQueuedLogs>100000</maxQueuedLogs> -->
        <!-- <maxLogsPerBatch>10000</maxLogsPerBatch> -->
        <!-- <maxTerminationTimeSeconds>10</maxTerminationTimeSeconds> -->
        <!-- <flushIntervalSeconds>1</flushIntervalSeconds> -->
        <!-- <maxScheduledLogsToBeAppended>1000</maxScheduledLogsToBeAppended> -->
    </appender>
```

See the [forwarder README](../forwarder/README.md#configuration) for a full description of the `NewRelicHttpAppender` config properties shown above.

### 3. `NewRelicAsyncAppender` must wrap any appenders that will target New Relic's log forwarder

Update your logging configuration xml to add this section. Change `"LOG_FILE"` to the `name` of the appender
you updated in the previous step.

```xml
    <appender name="ASYNC" class="com.newrelic.logging.logback.NewRelicAsyncAppender">
        <appender-ref ref="LOG_FILE" />
    </appender>
```

*Why?* The New Relic log format includes New Relic-specific data that must be captured on the thread the log message
is coming from. This appender captures that information before passing to the standard `AsyncAppender` logic. 

### 4. The Async Appender must be referenced by all loggers

Update your logging configuration xml to connect the root (and other) loggers to the `ASYNC` appender you configured
in the previous step.

```xml
   <root level="INFO">
       <appender-ref ref="ASYNC" />
   </root>
```

## Adding Custom Arguments to JSON Log

As of `com.newrelic.logging:logback:2.3` you can add custom arguments in the JSON log for logback as follows:

```
import static com.newrelic.logging.logback.CustomArgument.keyValue;
logger.info("Custom log", keyValue("customKey", "customValue"));
```
