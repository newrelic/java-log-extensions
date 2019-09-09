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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.newrelic.logging.core.ElementName.CLASS_NAME;
import static com.newrelic.logging.core.ElementName.LOGGER_NAME;
import static com.newrelic.logging.core.ElementName.LOG_LEVEL;
import static com.newrelic.logging.core.ElementName.MESSAGE;
import static com.newrelic.logging.core.ElementName.METHOD_NAME;
import static com.newrelic.logging.core.ElementName.THREAD_NAME;
import static com.newrelic.logging.core.ElementName.TIMESTAMP;
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
    void shouldIncludeMDCProperties() throws Exception {
        givenMockReturnsAnOpaqueValue();
        givenAppenderChainIsConfiguredForOneMessage();

        MDC.put("an.mdc.key", "an.mdc.value");
        Logger.getLogger("foo").log(Level.WARN, "hello, i'm another message");
        MDC.put("an.mdc.key", "some wacky value that changed _after_ we logged!");

        whenOneMessageIsConfirmedLogged();

        LogAsserts.assertFieldValues(innerAppender.appendedStrings.get(0), ImmutableMap.<String, Object>builder()
                .put(MESSAGE, "hello, i'm another message")
                .put(LOGGER_NAME, "foo")
                .put(LOG_LEVEL, "WARN")
                .put(THREAD_NAME, Thread.currentThread().getName())
                .put(CLASS_NAME, getClass().getName())
                .put(METHOD_NAME, "shouldIncludeMDCProperties")
                .put("an.opaque", "value")
                .put("an.mdc.key", "an.mdc.value")
                .build()
        );
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
