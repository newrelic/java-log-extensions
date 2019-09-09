# The New Relic Dropwizard Logging Extension

## Preconditions

1. Dropwizard must be configured and working in the application with the Dropwizard appenders and logging factory.
2. The New Relic Java agent must be enabled using the `-javaagent` command-line parameter.
3. You must be using at least version 5.6.0 of the Java Agent.

## Configuring

There are some changes to your application to use the New Relic Dropwizard Logging Extension. All steps are required.

### 1. Include the dependency in your project.

Gradle:

```groovy
dependencies {
    compile("com.newrelic:logging:dropwizard:1.0-rc1")
}
```

Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.newrelic.logging</groupId>
        <artifactId>dropwizard</artifactId>
        <version>1.0-rc1</version>
    </dependency>  
</dependencies>
```

### 2. Add services to `src/main/resources/META-INF/services`.

Create the following two files in the `src/main/resources/META-INF/services` directory. If either file already exists, 
append the contents below into the existing file.

#### File Name: `io.dropwizard.logging.AppenderFactory`
```
com.newrelic.logging.dropwizard.NewRelicConsoleAppenderFactory
com.newrelic.logging.dropwizard.NewRelicFileAppenderFactory
```

#### File Name: `io.dropwizard.logging.layout.DiscoverableLayoutFactory`
```
com.newrelic.logging.dropwizard.NewRelicJsonLayoutFactory
com.newrelic.logging.dropwizard.LogFormatLayoutFactory
```

*Why?* These files are configuration for a [ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
used by Dropwizard; it's how Dropwizard finds the layout and appenders used in the next step.

### 3. Use the `newrelic-console` or `newrelic-file` appender with a `newrelic-json` layout.

Update your DW configuration yaml like the example below. Modify the appender you have chosen to receive decorated logs.

If you were using `type: console`, then replace that with `type: newrelic-console`. This is a frequent use case for container-based applications. All
[configuration elements for `type: console`](https://www.dropwizard.io/1.3.13/docs/manual/configuration.html#console)
will still apply.

If you were using `type: file` then replace that with `type: newrelic-file`. All 
[configuration elements for `type: file`](https://www.dropwizard.io/1.3.13/docs/manual/configuration.html#file) 
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

--------------
Dropwizard is © Copyright 2010-2013, Coda Hale, Yammer Inc., 2014-2017 Dropwizard Team.