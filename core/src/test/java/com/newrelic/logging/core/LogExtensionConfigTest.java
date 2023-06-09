package com.newrelic.logging.core;

import org.junit.jupiter.api.Test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.newrelic.logging.core.ExceptionUtil.MAX_STACK_SIZE_DEFAULT;
import static com.newrelic.logging.core.LogExtensionConfig.ADD_MDC_DEFAULT;
import static com.newrelic.logging.core.LogExtensionConfig.ADD_MDC_ENV_VAR;
import static com.newrelic.logging.core.LogExtensionConfig.ADD_MDC_SYS_PROP;
import static com.newrelic.logging.core.LogExtensionConfig.MAX_STACK_SIZE_ENV_VAR;
import static com.newrelic.logging.core.LogExtensionConfig.MAX_STACK_SIZE_SYS_PROP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogExtensionConfigTest {

    @Test
    void testGetMaxStackSizeDefault() {
        int actualSize = LogExtensionConfig.getMaxStackSize();
        assertEquals(MAX_STACK_SIZE_DEFAULT, actualSize);
    }

    @Test
    void testGetMaxStackSizeSysProp() {
        int expectedSize = 5;
        System.setProperty(MAX_STACK_SIZE_SYS_PROP, String.valueOf(expectedSize));
        int actualSize = LogExtensionConfig.getMaxStackSize();
        assertEquals(expectedSize, actualSize);

        System.clearProperty(MAX_STACK_SIZE_SYS_PROP);
    }

    @Test
    void testGetMaxStackSizeEnvVar() throws Exception {
        int expectedSize = 7;
        int actualSize = withEnvironmentVariable(MAX_STACK_SIZE_ENV_VAR, String.valueOf(expectedSize)).execute(LogExtensionConfig::getMaxStackSize);
        assertEquals(expectedSize, actualSize);
    }

    @Test
    void testGetMaxStackSizeEnvVarAndSysProp() throws Exception {
        // Set via sys prop
        int sysPropSize = 90;
        System.setProperty(MAX_STACK_SIZE_SYS_PROP, String.valueOf(sysPropSize));

        // Environment variable takes precedence over system property
        int expectedSize = 3;
        int actualSize = withEnvironmentVariable(MAX_STACK_SIZE_ENV_VAR, String.valueOf(expectedSize)).execute(LogExtensionConfig::getMaxStackSize);
        assertEquals(expectedSize, actualSize);

        System.clearProperty(MAX_STACK_SIZE_SYS_PROP);
    }

    @Test
    void testShouldAddMDCDefault() {
        boolean actualAddMDCValue = LogExtensionConfig.shouldAddMDC();
        assertEquals(ADD_MDC_DEFAULT, actualAddMDCValue);
    }

    @Test
    void testShouldAddMDCSysProp() {
        boolean expectedAddMDCValue = true;
        System.setProperty(ADD_MDC_SYS_PROP, String.valueOf(expectedAddMDCValue));
        boolean actualAddMDCValue = LogExtensionConfig.shouldAddMDC();
        assertEquals(expectedAddMDCValue, actualAddMDCValue);

        System.clearProperty(ADD_MDC_SYS_PROP);
    }

    @Test
    void testShouldAddMDCEnvVar() throws Exception {
        boolean expectedAddMDCValue = true;
        boolean actualAddMDCValue = withEnvironmentVariable(ADD_MDC_ENV_VAR, String.valueOf(expectedAddMDCValue)).execute(LogExtensionConfig::shouldAddMDC);
        assertEquals(expectedAddMDCValue, actualAddMDCValue);
    }

    @Test
    void testShouldAddMDCEnvVarAndSysProp() throws Exception {
        // Set via sys prop
        boolean sysPropAddMDCValue = false;
        System.setProperty(ADD_MDC_SYS_PROP, String.valueOf(sysPropAddMDCValue));

        // Environment variable takes precedence over system property
        boolean expectedAddMDCValue = true;
        boolean actualAddMDCValue = withEnvironmentVariable(ADD_MDC_ENV_VAR, String.valueOf(expectedAddMDCValue)).execute(LogExtensionConfig::shouldAddMDC);
        assertEquals(expectedAddMDCValue, actualAddMDCValue);

        System.clearProperty(ADD_MDC_SYS_PROP);
    }

    @Test
    void isInteger() {
        assertFalse(LogExtensionConfig.isInteger("FOO"));
        assertFalse(LogExtensionConfig.isInteger("90.12"));
        assertTrue(LogExtensionConfig.isInteger("200"));
    }
}