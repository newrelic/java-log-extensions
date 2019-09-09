/**
 * The New Relic Log plugin for Logback.
 *
 * Use the public classes in your logging config xml. The {@link com.newrelic.logging.logback.NewRelicEncoder} must be
 * used as an encoder for the New Relic JSON format. The {@link com.newrelic.logging.logback.NewRelicAsyncAppender} must
 * be used to wrap the appender so that the encoder can capture the trace and span information at the time of the
 * log message.
 *
 * @see <a href="https://logback.qos.ch/documentation.html">Logback Documentation</a>
 * @see <a href="https://source.datanerd.us/java-agent/log-plugins/blob/master/logback-plugin/README.md">Project README</a>
 */
package com.newrelic.logging.logback;
