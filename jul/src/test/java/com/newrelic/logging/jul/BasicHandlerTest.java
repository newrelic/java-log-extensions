package com.newrelic.logging.jul;

import org.junit.jupiter.api.Test;

/**
 * This class must contain only one test because JUL can't be reset except in a different JVM process.
 */
class BasicHandlerTest extends HandlerTestBase {
    @Test
    void baseBehaviorIsCorrect() throws Exception {
        givenAConfiguredLogger();
        whenAMessageIsDefinitelyLogged();
        thenBasicValuesArePresent();
    }
}
