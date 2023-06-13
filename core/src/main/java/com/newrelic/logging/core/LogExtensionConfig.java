/*
 * Copyright 2023 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.logging.core;

public class LogExtensionConfig {
    public static final String CONTEXT_PREFIX = "context.";
    private static final String CONFIG_PREFIX_ENV_VAR = "NEW_RELIC_LOG_EXTENSION_";
    private static final String CONFIG_PREFIX_SYS_PROP = "newrelic.log_extension.";
    public static final String MAX_STACK_SIZE_ENV_VAR = CONFIG_PREFIX_ENV_VAR + "MAX_STACK_SIZE";
    public static final String MAX_STACK_SIZE_SYS_PROP = CONFIG_PREFIX_SYS_PROP + "max_stack_size";
    public static final String ADD_MDC_ENV_VAR = CONFIG_PREFIX_ENV_VAR + "ADD_MDC";
    public static final String ADD_MDC_SYS_PROP = CONFIG_PREFIX_SYS_PROP + "add_mdc";
    public static final boolean ADD_MDC_DEFAULT = false;

    /**
     * Get an int representing the max stack size for errors that should be added to logs
     * <p>
     * Precedence: Env var > Sys prop > Default
     *
     * @return int representing max stack size
     */
    public static int getMaxStackSize() {
        String envVar = System.getenv(MAX_STACK_SIZE_ENV_VAR);
        String sysProp = System.getProperty(MAX_STACK_SIZE_SYS_PROP);

        if (isInteger(envVar)) {
            return Integer.parseInt(envVar);
        } else if (isInteger(sysProp)) {
            return Integer.parseInt(sysProp);
        } else {
            return ExceptionUtil.MAX_STACK_SIZE_DEFAULT;
        }
    }

    /**
     * Get a boolean indicating if MDC should be added to logs
     * <p>
     * Precedence: Env var > Sys prop > Default
     *
     * @return true if MDC should be added to logs, else false
     */
    public static boolean shouldAddMDC() {
        String envVar = System.getenv(ADD_MDC_ENV_VAR);
        String sysProp = System.getProperty(ADD_MDC_SYS_PROP);

        if (envVar != null) {
            return Boolean.parseBoolean(envVar);
        } else if (sysProp != null) {
            return Boolean.parseBoolean(sysProp);
        } else {
            return ADD_MDC_DEFAULT;
        }
    }

    /**
     * Validate that a String value evaluates to an integer
     *
     * @param val String to be evaluated as an int
     * @return true if val is an int, else false
     */
    static boolean isInteger(String val) {
        if (val == null) {
            return false;
        }
        try {
            Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
