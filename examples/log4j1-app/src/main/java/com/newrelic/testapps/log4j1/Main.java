/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.testapps.log4j1;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("\uD83C\uDF89 Starting the program.");

        List<Thread> threads = Arrays.asList(
                new Thread(() -> transactionWithError("Here is an error")),
                new Thread(() -> transactionWithWarn("Here is a warning")),
                new Thread(() -> transactionWithInfo("Here is an informational message")),
                new Thread(Main::transactionWithAsyncComponent)
        );

        threads.forEach(Thread::start);
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        logger.info("Program complete.");
    }

    @Trace(dispatcher = true)
    private static void transactionWithInfo(String message) {
        logger.info(message);
    }

    @Trace(dispatcher = true)
    private static void transactionWithWarn(String message) {
        logger.warn(message);
        callSecondaryTrace();
    }

    @Trace
    private static void callSecondaryTrace() {
        logger.error("This is a secondary error called by the warning transaction.");
    }

    @Trace(dispatcher = true)
    private static void transactionWithError(String message) {
        logger.error(message);
        Throwable t = new RuntimeException("Whoops!");
        NewRelic.noticeError(t);
        logger.error("this contains a throwable", t);
        logger.error("This is a secondary error in the same span as the error message");
    }

    @Trace(dispatcher = true)
    private static void transactionWithAsyncComponent() {
        logger.error(MessageFormat.format("This is the first message from an async transaction using {0} passing", "token"));
        final Token token = NewRelic.getAgent().getTransaction().getToken();
        Thread t = new Thread(() -> theAsyncMethod(token));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.warn("Resuming the sync method after the async method");
    }

    @Trace(async = true)
    private static void theAsyncMethod(Token token) {
        logger.error("The actual async method: token linkAndExpire success? " + token.linkAndExpire());
    }

}
