# The New Relic Logback 1.3.x Extension

## Preconditions

1. logback 1.3.x must be configured and working in the application.
2. The New Relic Java agent must be enabled using the `-javaagent` command-line parameter.
3. You must be using at least version 8.21.0 of the Java Agent.

## Configuring

There are some required changes to your application's logging configuration to use the New Relic
Logback Extension for Logback-1.3. All steps are required.

**Optional**: [Configuration Options](../README.md#configuration-options) for collecting MDC or controlling stack trace behavior.

---

### 1. Include the dependency in your project.

Refer to [Maven Central](https://search.maven.org/search?q=g:com.newrelic.logging%20a:logback13) for the appropriate snippets.

---

### 2. Configure an `<appender>` element with a `NewRelicEncoder`.

Update your `logback.xml` to include the `<encoder>` element like below.

```xml
    <appender name="LOG_FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/app-log-file.log</file>
        <encoder class="com.newrelic.logging.logback13.NewRelicEncoder"/>
    </appender>
```

*Why?* The New Relic log format is a tailored JSON format with specific fields in specific places
that our log forwarder plugins and back end rely on. At this time, we don't support any customization
of that format.

### 3. `NewRelicAsyncAppender` must wrap any appenders that will target New Relic's log forwarder

Update your logging configuration xml to add this section. Change `"LOG_FILE"` to the `name` of the appender
you updated in the previous step.

```xml
    <appender name="ASYNC" class="com.newrelic.logging.logback13.NewRelicAsyncAppender">
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
