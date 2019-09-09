/*
 * Copyright 2019. New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.log4j2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class MessageFactoryTest {
    @Test
    void existingMethodsShouldGenerateNewRelicMessageObjects() {
        NewRelicMessageFactory target = new NewRelicMessageFactory();

        assertInstance(target.newMessage("Here's a string"));
        assertInstance(target.newMessage(new StringBuilder().append("Here's a StringBuilder")));
        assertInstance(target.newMessage(null));
        assertInstance(target.newMessage(Boolean.TRUE));
        assertInstance(target.newMessage("String with subs {} {} {}", "sub1", "sub2", "sub3"));
        assertInstance(target.newMessage("String with subs {} {} {} {} {} {} {} {} {}",
                "sub1", "sub2", "sub3","sub1", "sub2", "sub3","sub1", "sub2", "sub3"));
    }

    private void assertInstance(Object obj) {
        assertNotNull(obj, "obj should not be null");
        if (!(obj instanceof NewRelicMessage)) {
            fail("obj should be NewRelicMessage, not " + obj.getClass());
        }
    }
}
