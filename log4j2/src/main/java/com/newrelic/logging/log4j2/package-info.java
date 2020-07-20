/**
 * The New Relic Log plugin for Apache Log4j 2.x.
 *
 * The {@link com.newrelic.logging.log4j2.NewRelicMessageFactory} must be assigned to the {@literal log4j2.messageFactory} system property.
 * The {@link com.newrelic.logging.log4j2.NewRelicLayout} must be used as a layout for the appender whose log data will end up forwarded
 * to New Relic.
 *
 * Any custom implementations of {@link org.apache.logging.log4j.message.Message} must derive from {@link com.newrelic.logging.log4j2.NewRelicMessage}.
 * Any custom implementations of {@link org.apache.logging.log4j.message.MessageFactory2} must derive from
 * {@link com.newrelic.logging.log4j2.NewRelicMessageFactory}.
 *
 * @see <a href="https://logging.apache.org/log4j/2.x/">Apache Log4j</a>
 */
package com.newrelic.logging.log4j2;
