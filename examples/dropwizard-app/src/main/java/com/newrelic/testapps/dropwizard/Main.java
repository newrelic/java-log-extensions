/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.testapps.dropwizard;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.logging.dropwizard.access.LinkingMetadataAsRequestAttributesFilter;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Main  extends Application<AppConfiguration> {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

    @Override
    public void run(AppConfiguration configuration, Environment environment) {
        // Enable MDC collection
        // Alternatively, this could be set using the environment variable NEW_RELIC_LOG_EXTENSION_ADD_MDC
        System.setProperty("newrelic.log_extension.add_mdc", "true");

        logger.info("\uD83C\uDF89 Starting the program.");

        // Add MDC data
        MDC.put("contextKey1", "contextData1");
        MDC.put("contextKey2", "contextData2");
        MDC.put("contextKey3", "contextData3");

        // Set up our resource so Jersey can find it.
        environment.jersey().register(new Hello());

        // This is step 2 of 2 for using New Relic linking metadata with Dropwizard request logging.
        // This filter will add most linking metadata as request attributes. From there, it can be
        // included in the newrelic-access-json format, or in a customized PatternLayout under %requestAttribute.
        environment.servlets().addFilter("trace-into-request", new LinkingMetadataAsRequestAttributesFilter())
                .addMappingForUrlPatterns(null, true, "/*");

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

        // Clear MDC data
        MDC.clear();
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
