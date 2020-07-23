# The New Relic Dropwizard Logging Extension

## Preconditions

1. Dropwizard must be configured and working in the application with the Dropwizard appenders and logging factory.
2. The New Relic Java agent must be enabled using the `-javaagent` command-line parameter.
3. You must be using at least version 5.6.0 of the Java Agent.

## Configuring

There are some changes to your application to use the New Relic Dropwizard Logging Extension. All steps are required.

### 1. Include the dependency in your project.

Refer to [Maven Central](https://search.maven.org/search?q=g:com.newrelic.logging%20a:dropwizard) for the appropriate snippets.

### 2. Use the `newrelic-console` or `newrelic-file` appender with a `newrelic-json` layout.

Update your DW configuration yaml like the example below. Modify the appender you have chosen to receive decorated logs.

If you were using `type: console`, then replace that with `type: newrelic-console`. This is a frequent use case for container-based applications. All
[configuration elements for `type: console`](https://dropwizard.readthedocs.io/en/release-1.3.x/manual/configuration.html#console)
will still apply.

If you were using `type: file` then replace that with `type: newrelic-file`. All 
[configuration elements for `type: file`](https://dropwizard.readthedocs.io/en/release-1.3.x/manual/configuration.html#file) 
will still apply.

```yaml
logging:
  appenders:
    - type: newrelic-file
      # Add the two lines below if you don't have a layout specified on the appender.
      # If you do have a layout, remove all parameters to the layout and set the type to newrelic-json.
      layout: 
        type: newrelic-json
```

*Why?* The appenders are different because they must capture New Relic-specific data on the thread the log message
is coming from. The `newrelic-file` and `newrelic-console` appenders inherit from the existing `file` and `console`
appenders with a different asynchronous wrapper. Unfortunately, DW service loading does not provide for injecting 
that automatically.

The layout is different because the New Relic log format is a tailored JSON format with specific fields in specific places
that our log forwarder plugins and back end rely on.

## Fallback layout type

The New Relic Dropwizard plugin also supports a `log-format` layout type that uses the standard Dropwizard logging. For testing purposes,
you can change the type of the layout with a one-line change.

```yaml
logging:
  appenders:
    - type: newrelic-file
      # This format will be ignored by the `newrelic-json` layout, but used by the `log-format` layout.
      logFormat: "%date{ISO8601} %c %-5p: %m trace.id=%mdc{trace.id} span.id=%mdc{span.id}%n"
      layout: 
#        type: newrelic-json
        type: log-format
```

## Adding linking metadata to the request log

There are two important steps for adding linking metadata to the request log.

### Include the `LinkingMetadataAsRequestAttributesFilter` for all resources

```java
    @Override
    public void run(AppConfiguration configuration, Environment environment) {
        environment.servlets().addFilter("trace-into-request", new LinkingMetadataAsRequestAttributesFilter())
                .addMappingForUrlPatterns(null, true, "/*");
    }
```

### Configure your request log to use `newrelic-access-json`

This layout type is a JSON format stylized after OpenTelemetry attributes. _Do not_ use a `newrelic-` appender, only use the `newrelic-access-json` layout.

```yaml
server:
  requestLog:
    appenders:
      - type: console
        layout:
          type: newrelic-access-json
```

--------------
Dropwizard is © Copyright 2010-2013, Coda Hale, Yammer Inc., 2014-2017 Dropwizard Team.
