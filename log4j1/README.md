# The New Relic log4j 1.x Extension

## Preconditions

1. log4j 1.x must be configured and working in the application.
2. log4j must be configured in code or via XML. Properties files are not supported because
`AsyncAppender` instances can only be automatically configured via XML.
3. You must be using at least version 5.6.0 of the Java Agent.

## Configuring

There are some changes to your application to use the New Relic
log4j 1.x Extension. All steps are required.

### 1. Include the dependency in your project.

Refer to [Maven Central](https://search.maven.org/search?q=g:com.newrelic.logging%20a:log4j1) for the appropriate snippets.


### 2. Configure an `<appender>` element with a `NewRelicLayout`.

Update your logging configuration xml like the example below.

```xml
    <appender name="TypicalFile" class="org.apache.log4j.FileAppender">
        <param name="file" value="logs/log4j1-app.log"/>
        <param name="append" value="false"/>
        <layout class="com.newrelic.logging.log4j1.NewRelicLayout"/> <!-- only this line needs to be added -->
    </appender>
```

*Why?* The New Relic log format is a tailored JSON format with specific fields in specific places
that our log forwarder plugins and back end rely on. At this time, we don't support any customization
of that format.

### 3. `NewRelicAsyncAppender` must wrap any appenders that will target New Relic's log forwarder

Update your logging configuration XML like the example below.

```xml
    <appender name="NewRelicFile" class="com.newrelic.logging.log4j1.NewRelicAsyncAppender">
        <appender-ref ref="TypicalFile" />
    </appender>
```

*Why?* The New Relic log format includes New Relic-specific data that must be captured on the thread the log message
is coming from. This appender captures that information before passing to the standard `AsyncAppender` logic.
