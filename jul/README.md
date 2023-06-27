# The New Relic `java.util.logging` Extension

## Preconditions

1. `java.util.logging` must be configured and working in the application.
2. The New Relic Java agent must be enabled using the `-javaagent` command-line parameter.
3. You must be using at least version 5.6.0 of the Java Agent.

## Configuring

There are some changes to your application to use the New Relic
`java.util.logging` Extension. All steps are required.

**Optional**: [Configuration Options](..%2FREADME.md#configuration-options) for setting max stack size.
Note that JUL does not support collecting MDC and thus the optional MDC config options will have no effect.

### 1. Include the extension in your project.

Refer to [Maven Central](https://search.maven.org/search?q=g:com.newrelic.logging%20a:jul) for the appropriate snippets.

### 2. Use the `NewRelicMemoryHandler` to intercept messages destined for another handler.

Your logging properties file will include a line listing the root logger's handlers, like this:

```properties
# Your file will have a handlers property, but it might be set to something else.
handlers = java.util.logging.FileHandler 
```

Update your logging properties file set the root logger's handler.

```properties
handlers = com.newrelic.logging.jul.NewRelicMemoryHandler
```

Configure the `NewRelicMemoryHandler` by setting the target to the handler that was previously set for the `handlers` property.

```properties
# The value of this property should be the handler that was previously assigned to the root logger above.
com.newrelic.logging.jul.NewRelicMemoryHandler.target = java.util.logging.FileHandler
```

*Why?* The New Relic log format includes data from the New Relic agent that must be captured on the thread the log message
is coming from. This `MemoryHandler` implementation intercepts that data.  

### 3. Use a `NewRelicFormatter` for the final handler.

Update your logging properties file to set the `formatter` property like the example below.
**Note**: the handler on which you set the formatter must be the `target` handler from the
previous step (`java.util.logging.FileHandler` in this example).

```properties
java.util.logging.FileHandler.formatter = com.newrelic.logging.jul.NewRelicFormatter
```

*Why?* The New Relic log format is a tailored JSON format with specific fields in specific places
that our log forwarder plugins and back end rely on. At this time, we don't support any customization
of that format.
