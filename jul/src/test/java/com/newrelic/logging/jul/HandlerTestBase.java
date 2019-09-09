package com.newrelic.logging.jul;

import com.google.common.collect.ImmutableMap;
import com.newrelic.logging.core.LogAsserts;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static com.newrelic.logging.core.ElementName.CLASS_NAME;
import static com.newrelic.logging.core.ElementName.LOGGER_NAME;
import static com.newrelic.logging.core.ElementName.LOG_LEVEL;
import static com.newrelic.logging.core.ElementName.MESSAGE;
import static com.newrelic.logging.core.ElementName.METHOD_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Derived classes must contain only one test because JUL can't be reset except in a different JVM process.
 */
abstract class HandlerTestBase {
    @AfterEach
    void tearDown() {
        fakeTestLogger = null;
        LogManager.getLogManager().reset();
    }

    @AfterAll
    static void tearDownClass() {
        ListHandler.lastInstance = null;
    }

    void givenAConfiguredLogger() throws IOException {
        String configuration = ""
                + "FakeTestLogger.handlers = com.newrelic.logging.jul.NewRelicMemoryHandler\n"
                + "com.newrelic.logging.jul.NewRelicMemoryHandler.level = ALL\n"
                + "com.newrelic.logging.jul.NewRelicMemoryHandler.push = ALL\n"
                + "com.newrelic.logging.jul.NewRelicMemoryHandler.target = com.newrelic.logging.jul.ListHandler\n"
                + "com.newrelic.logging.jul.ListHandler.formatter = com.newrelic.logging.jul.NewRelicFormatter\n"
                + "com.newrelic.logging.jul.ListHandler.expectedCount = 1\n";

        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(configuration.getBytes(StandardCharsets.UTF_8)));

        fakeTestLogger = Logger.getLogger("FakeTestLogger");
        assertEquals(1, fakeTestLogger.getHandlers().length);
    }

    void whenAMessageIsDefinitelyLogged() throws InterruptedException {
        fakeTestLogger.severe("i am a message");
        boolean latchCountedDown = ListHandler.lastInstance.latch.await(3, TimeUnit.SECONDS);
        assertTrue(latchCountedDown);
        assertEquals(1, ListHandler.lastInstance.capturedLogs.size());
    }

    void thenBasicValuesArePresent() throws IOException {
        LogAsserts.assertFieldValues(ListHandler.lastInstance.capturedLogs.get(0), ImmutableMap.<String, Object>builder()
                .put(MESSAGE, "i am a message")
                .put(LOG_LEVEL, "SEVERE")
                .put(CLASS_NAME, HandlerTestBase.class.getName())
                .put(METHOD_NAME, "whenAMessageIsDefinitelyLogged")
                .put(LOGGER_NAME, fakeTestLogger.getName())
                .build()
        );
    }

    private ListHandler createdListHandler = null;
    private Logger fakeTestLogger = null;
}
