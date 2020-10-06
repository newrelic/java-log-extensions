package com.newrelic.logging.logback;

import com.fasterxml.jackson.core.JsonFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JsonFactoryProviderTest {

    @Test
    void shouldAlwaysGetSameInstance(){
        JsonFactory instance1 = JsonFactoryProvider.getInstance();
        JsonFactory instance2 = JsonFactoryProvider.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2);
    }

}