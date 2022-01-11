# The New Relic log4j 2.x Extension

## Preconditions

1. log4j 2.13.2 or higher must be configured and working in the application.
2. The New Relic Java agent must be enabled using the `-javaagent` command-line parameter.
3. You must be using at least version 5.6.0 of the Java Agent.

## Configuring

There are some changes to your application to use the New Relic
log4j 2.x Extension. All steps are required.

### 1. Include the dependency in your project.

Refer to [Maven Central](https://search.maven.org/search?q=g:com.newrelic.logging%20a:log4j2) for the appropriate snippets.

### 2. Use a `NewRelicLayout` element within one of the appenders.

Update your logging configuration xml to add `<NewRelicLayout/>` like this:

```xml
        <File name="MyFile" fileName="logs/app-log-file.log">
            <NewRelicLayout/>
        </File>
```

or like this:

```xml
        <Console name="MyConsole">
            <NewRelicLayout/>
        </Console>
```

*Why?* The New Relic log format is a tailored JSON format with specific fields in specific places
        that our log forwarder plugins and back end rely on. At this time, we don't support any customization
        of that format.
```
