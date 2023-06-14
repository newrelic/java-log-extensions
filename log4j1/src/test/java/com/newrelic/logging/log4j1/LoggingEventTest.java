package com.newrelic.logging.log4j1;

import com.google.common.collect.ImmutableMap;
import com.newrelic.api.agent.Agent;
import com.newrelic.logging.core.LogAsserts;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.newrelic.logging.core.ElementName.CLASS_NAME;
import static com.newrelic.logging.core.ElementName.ERROR_CLASS;
import static com.newrelic.logging.core.ElementName.ERROR_MESSAGE;
import static com.newrelic.logging.core.ElementName.ERROR_STACK;
import static com.newrelic.logging.core.ElementName.LOGGER_NAME;
import static com.newrelic.logging.core.ElementName.LOG_LEVEL;
import static com.newrelic.logging.core.ElementName.MESSAGE;
import static com.newrelic.logging.core.ElementName.METHOD_NAME;
import static com.newrelic.logging.core.ElementName.THREAD_NAME;
import static com.newrelic.logging.core.ElementName.TIMESTAMP;
import static com.newrelic.logging.core.LogExtensionConfig.ADD_MDC_SYS_PROP;
import static com.newrelic.logging.core.LogExtensionConfig.CONTEXT_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingEventTest {
    @BeforeAll
    static void setUp() {
        cachedSupplier = NewRelicLoggingEvent.agentSupplier;
    }

    @AfterAll
    static void tearDown() {
        NewRelicLoggingEvent.agentSupplier = cachedSupplier;
    }

    @Test
    void shouldWork() throws Exception {
        givenMockReturnsAnOpaqueValue();
        givenAppenderChainIsConfiguredForOneMessage();

        LoggingEvent evt = new LoggingEvent("abc", Logger.getLogger("foo"), 123123123123L, Level.INFO,
                "hello i'm a message", null);

        targetAppender.append(evt);

        whenOneMessageIsConfirmedLogged();

        LogAsserts.assertFieldValues(innerAppender.appendedStrings.get(0), ImmutableMap.<String, Object>builder()
                .put(MESSAGE, "hello i'm a message")
                .put(LOGGER_NAME, "foo")
                .put(LOG_LEVEL, "INFO")
                .put(THREAD_NAME, Thread.currentThread().getName())
                .put(TIMESTAMP, 123123123123L)
                .put("an.opaque", "value")
                .build()
        );
    }

    @Test
    void shouldRenderExceptionFieldsJustFine() throws Exception {
        givenMockReturnsAnOpaqueValue();
        givenAppenderChainIsConfiguredForOneMessage();

        LoggingEvent evt = new LoggingEvent("abc", Logger.getLogger("foo"), 123123123123L, Level.INFO,
                "whoopsie", new Exception("~~ oops ~~"));

        targetAppender.append(evt);

        whenOneMessageIsConfirmedLogged();

        LogAsserts.assertFieldValues(innerAppender.appendedStrings.get(0), ImmutableMap.<String, Object>builder()
                .put(ERROR_CLASS, "java.lang.Exception")
                .put(ERROR_MESSAGE, "~~ oops ~~")
                .put(ERROR_STACK, Pattern.compile(".*LoggingEventTest\\.shouldRenderExceptionFieldsJustFine.*", Pattern.DOTALL))
                .build());
    }

    @Test
    void shouldNotIncludeMDCProperties() throws Exception {
        givenMockReturnsAnOpaqueValue();
        givenAppenderChainIsConfiguredForOneMessage();

        String mdcKey = "an.mdc.key";
        String prefixedMdcKey = CONTEXT_PREFIX + mdcKey;

        MDC.put(mdcKey, "an.mdc.value");
        Logger.getLogger("foo").log(Level.WARN, "hello, i'm another message");
        MDC.put(mdcKey, "some wacky value that changed _after_ we logged!");

        whenOneMessageIsConfirmedLogged();

        String result = innerAppender.appendedStrings.get(0);

        LogAsserts.assertFieldValues(result, ImmutableMap.<String, Object>builder()
                .put(MESSAGE, "hello, i'm another message")
                .put(LOGGER_NAME, "foo")
                .put(LOG_LEVEL, "WARN")
                .put(THREAD_NAME, Thread.currentThread().getName())
                .put(CLASS_NAME, getClass().getName())
                .put(METHOD_NAME, "shouldNotIncludeMDCProperties")
                .put("an.opaque", "value")
                .put(mdcKey, "an.mdc.value")
                .build()
        );

        // MDC collection is disabled by default, key should not exist
        LogAsserts.assertFieldExistence(prefixedMdcKey, result, false);
    }

    @Test
    void shouldIncludeMDCProperties() throws Exception {
        // Explicitly enable MDC collection
        System.setProperty(ADD_MDC_SYS_PROP, "true");

        givenMockReturnsAnOpaqueValue();
        givenAppenderChainIsConfiguredForOneMessage();

        String mdcKey = "an.mdc.key";
        String prefixedMdcKey = CONTEXT_PREFIX + mdcKey;

        MDC.put(mdcKey, "an.mdc.value");
        Logger.getLogger("foo").log(Level.WARN, "hello, i'm another message");
        MDC.put(mdcKey, "some wacky value that changed _after_ we logged!");

        whenOneMessageIsConfirmedLogged();

        String result = innerAppender.appendedStrings.get(0);

        LogAsserts.assertFieldValues(result, ImmutableMap.<String, Object>builder()
                .put(MESSAGE, "hello, i'm another message")
                .put(LOGGER_NAME, "foo")
                .put(LOG_LEVEL, "WARN")
                .put(THREAD_NAME, Thread.currentThread().getName())
                .put(CLASS_NAME, getClass().getName())
                .put(METHOD_NAME, "shouldIncludeMDCProperties")
                .put("an.opaque", "value")
                .put(mdcKey, "an.mdc.value")
                .build()
        );

        // MDC collection is explicitly enabled, key should exist
        LogAsserts.assertFieldExistence(prefixedMdcKey, result, true);
    }

    private void givenAppenderChainIsConfiguredForOneMessage() {
        innerAppender = new ListAppender(new NewRelicLayout(), 1);
        targetAppender = new NewRelicAsyncAppender();
        targetAppender.addAppender(innerAppender);

        LogManager.getRootLogger().removeAllAppenders();
        LogManager.getRootLogger().addAppender(targetAppender);
    }

    private void givenMockReturnsAnOpaqueValue() {
        final Agent mockAgent = Mockito.mock(Agent.class);
        Map<String, String> results = new HashMap<>();
        results.put("an.opaque", "value");
        Mockito.doReturn(results).when(mockAgent).getLinkingMetadata();
        NewRelicLoggingEvent.agentSupplier = () -> mockAgent;
    }

    private void whenOneMessageIsConfirmedLogged() throws InterruptedException {
        assertTrue(innerAppender.latch.await(3, TimeUnit.SECONDS), "Expected the latch to count down in time");
        assertEquals(1, innerAppender.appendedStrings.size());
    }

    private ListAppender innerAppender;
    private AsyncAppender targetAppender;
    private static Supplier<Agent> cachedSupplier;
}
