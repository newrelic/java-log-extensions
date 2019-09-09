/**
 * The New Relic Log plugin for {@link java.util.logging} (JUL).
 *
 * Use the public classes in your logging properties. The {@link com.newrelic.logging.jul.NewRelicMemoryHandler}
 * must be used as a {@link java.util.logging.Handler} to get thread-local trace metadata. The {@link com.newrelic.logging.jul.NewRelicFormatter}
 * must be used as a formatter that the {@link com.newrelic.logging.jul.NewRelicMemoryHandler} eventually appends to.
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/logging/overview.html">Java Logging Overview</a>
 * @see <a href="https://source.datanerd.us/java-agent/log-plugins/blob/master/jdklogging-plugin/README.md">Project README</a>
 */
package com.newrelic.logging.jul;
