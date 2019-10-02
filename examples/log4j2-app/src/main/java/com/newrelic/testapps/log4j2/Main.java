/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.testapps.log4j2;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.LocalizedMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Supplier;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static ResourceBundle bundle = ResourceBundle.getBundle("propres");

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
        logger.warn((Supplier<String>) () -> bundle.getString("MSG123"));
        callSecondaryTrace();
    }

    @Trace
    private static void callSecondaryTrace() {
        logger.error("This is a secondary error called by the warning transaction.");
    }

    @Trace(dispatcher = true)
    private static void transactionWithError(String message) {
        logger.error(message);
        NewRelic.noticeError(new RuntimeException("Whoops!"));
        logger.error("This is a secondary error in the same span as the error message");

        // this cast only exists because log4j 2.8 has a deprecated MessageSupplier overload.
        // Lambda type inference was going to MessageSupplier, not Supplier.
        logger.error((Supplier<Message>)() -> new LocalizedMessage(bundle, "MSI123", "inserts (but no NR data!)"));
    }

    @Trace(dispatcher = true)
    private static void transactionWithAsyncComponent() {
        logger.error("This is the first message from an async transaction using {} passing", "token");
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
