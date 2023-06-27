## Examples

The included examples provide a `run` task (or `start` for dropwizard) that executes
a simple Main that has a few methods for logging. You can inspect the code and
configuration to see how our examples are configured. They all log to the console
and log to a separate file.

### Set up

You must follow some initial steps:

1. Obtain a New Relic license key by signing up for a trial account or using your paid account.
2. Download the newrelic.jar and newrelic.yml from http://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/.
3. Create a `lib` directory as a sibling of `examples`, and copy the files there.
4. Configure the yml with your license key. **Make sure to also turn distributed_tracing to enabled: true**

### Dropwizard

Run the application with `./gradlew :examples:dropwizard-app:start`. Some early log messages include transactions.
The decorated log messages will be written to `examples/dropwizard-app/logs/my-app.log`. See [test.yml](dropwizard-app/test.yml) for the configuration.

### `java.util.logging`

Run the application with `./gradlew :examples:jul-app:run`. When the application completes, 
look in `examples/jul-app/logs/myApp.log` for the decorated log messages.
See [logging.properties](jul-app/src/main/resources/logging.properties) for the configuration. 

### log4j 1.x

Run the application with `./gradlew :examples:log4j1-app:run`. The decorated log messages should be written to the console.
See [log4j1.xml](log4j1-app/src/main/resources/log4j.xml) for the configuration.

### log4j 2.x

Run the application with `./gradlew :examples:log4j2-app:run`. The decorated log messages will be written to 
`examples/log4j2-app/logs/log4j2.log`. See [log4j2.xml](log4j2-app/src/main/resources/log4j2.xml) for the configuration.

### logback

Run the application with `./gradlew :examples:logback-app:run`. The decorated log messages will be written to 
`examples/logback-app/logs/logback-app.log`. See [logback.xml](logback-app/src/main/resources/logback.xml) for the configuration.

### logback11

Run the application with `./gradlew :examples:logback11-app:run`. The decorated log messages will be written to 
`examples/logback11-app/logs/logback11-app.log`. See [logback.xml](logback11-app/src/main/resources/logback.xml) for the configuration.