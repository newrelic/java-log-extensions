/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.testapps.jul;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    private static Logger logger;
    private static ResourceBundle bundle = ResourceBundle.getBundle("propres");

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration();
        logger = Logger.getLogger(Main.class.getCanonicalName());
        LogManager.getLogManager().addLogger(logger);

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
        logger.warning(message);
        logger.warning(bundle.getString("MSG123"));
        callSecondaryTrace();
    }

    @Trace
    private static void callSecondaryTrace() {
        logger.severe("This is a secondary error called by the warning transaction.");
    }

    @Trace(dispatcher = true)
    private static void transactionWithError(String message) {
        logger.severe(message);
        Throwable t = new RuntimeException("Whoops!");
        NewRelic.noticeError(t);
        logger.log(Level.SEVERE, "this contains a throwable", t);
        logger.severe("This is a secondary error in the same span as the error message");
        logger.severe(() -> MessageFormat.format(bundle.getString("MSI123"), "inserts"));
    }

    @Trace(dispatcher = true)
    private static void transactionWithAsyncComponent() {
        logger.log(Level.SEVERE, "This is the first message from an async transaction using {0} passing", "token");
        final Token token = NewRelic.getAgent().getTransaction().getToken();
        Thread t = new Thread(() -> theAsyncMethod(token));
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.warning("Resuming the sync method after the async method");
    }

    @Trace(async = true)
    private static void theAsyncMethod(Token token) {
        logger.severe("The actual async method: token linkAndExpire success? " + token.linkAndExpire());
    }
}
