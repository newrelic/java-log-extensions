# The New Relic log4j 2.x Extension

## Preconditions

1. log4j 2.x must be configured and working in the application.
2. The New Relic Java agent must be enabled using the `-javaagent` command-line parameter.
3. You must be using at least version 5.6.0 of the Java Agent.

## Configuring

There are some changes to your application to use the New Relic
log4j 2.x Extension. All steps are required.

### 1. Include the dependency in your project.

Gradle:

```groovy
dependencies {
    compile("com.newrelic.logging:log4j2:1.0-rc1")
}
```

Maven:

```xml
<dependencies>
    <dependency>
        <groupId>com.newrelic.logging</groupId>
        <artifactId>log4j2</artifactId>
        <version>1.0-rc1</version>
    </dependency>  
</dependencies>
```

### 2. Add `packages="com.newrelic.logging.log4j2"` to the `Configuration` element.

Update your logging configuration xml to add a `packages` attribute.

```xml
<Configuration xmlns="http://logging.apache.org/log4j/2.0/config"
               packages="com.newrelic.logging.log4j2">
```

*Why?* This attribute tells the XML parser that some classes are found in a non-standard package.

### 3. Use a `NewRelicLayout` element within one of the appenders.

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

### 4. Set the `log4j2.messageFactory` property to the `NewRelicMessageFactory`.

The exact method will vary depending on the application framework. One option is to change
the [`java` command-line with `-D`](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABDJJFI).
You may also have application properties files in your project.

```properties
log4j2.messageFactory=com.newrelic.logging.log4j2.NewRelicMessageFactory
```

*Why?* The New Relic log format includes New Relic-specific data that must be captured on the thread the log message
is coming from. This `MessageFactory` instance is the log4j 2.x hook to do this.
