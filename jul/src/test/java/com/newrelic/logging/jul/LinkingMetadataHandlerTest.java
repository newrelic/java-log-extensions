package com.newrelic.logging.jul;

import com.google.common.collect.ImmutableMap;
import com.newrelic.api.agent.Agent;
import com.newrelic.logging.core.LogAsserts;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * This class must contain only one test because JUL can't be reset except in a different JVM process.
 */
public class LinkingMetadataHandlerTest extends HandlerTestBase {
    @Test
    void shouldAddLinkingMetadataToMessage() throws Exception {
        givenFakeLinkingMetadata();
        givenAConfiguredLogger();
        whenAMessageIsDefinitelyLogged();
        thenBasicValuesArePresent();
        thenFakeLinkingMetadataShouldBePresent();
    }

    void givenFakeLinkingMetadata() {
        Mockito.doReturn(ImmutableMap.of("an.opaque.key", "value1", "another.key", "value2"))
                .when(NewRelicLogRecord.agentSupplier.get()).getLinkingMetadata();
    }

    void thenFakeLinkingMetadataShouldBePresent() throws IOException {
        LogAsserts.assertFieldValues(ListHandler.lastInstance.capturedLogs.get(0), ImmutableMap.<String, Object>builder()
                .put("an.opaque.key", "value1")
                .put("another.key", "value2")
                .build()
        );
    }

    @BeforeAll
    static void lmSetUp() {
        cachedSupplier = NewRelicLogRecord.agentSupplier;
        Agent mockAgent = Mockito.spy(Agent.class);
        NewRelicLogRecord.agentSupplier = () -> mockAgent;
    }

    @AfterAll
    static void lmTearDown() {
        NewRelicLogRecord.agentSupplier = cachedSupplier;
    }

    private static Supplier<Agent> cachedSupplier;
}
