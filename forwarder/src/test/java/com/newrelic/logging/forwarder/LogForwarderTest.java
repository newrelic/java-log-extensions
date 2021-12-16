package com.newrelic.logging.forwarder;

import ch.qos.logback.classic.Level;
import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.Config;
import com.newrelic.telemetry.TelemetryClient;
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.logs.LogBatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class LogForwarderTest {

    private static final int FIVE_SECONDS = 5_000;

    private static final Log LOG = Log.builder().level(Level.ERROR.levelStr).message("a sample error log").build();

    private LogForwarder forwarder;
    private TelemetryClient telemetryClient;
    private LogBatch sentLogBatch;

    @Test
    @Timeout(3)
    void shouldWorkCorrectly() {
        givenAMockedTelemetryClient();
        givenAStartedForwarderInstanceWithDefaultConfiguration();
        whenAppendingALog(LOG);
        thenSendBatchFromTelemetryClientShouldBeCalled(1);
        thenSentBatchShouldContainTheLog(LOG);
    }

    @Test
    @Timeout(3)
    void shouldEmitNotificationErrorIfAnErrorOccurs() {
        givenAMockedTelemetryClient();
        givenAStartedForwarderInstanceWithDefaultConfiguration();
        whenAppendingALog(LOG);
    }

    @Test
    @Timeout(10)
    void shouldCreateANewLogBatchIfMaxLogsPerBatchIsReached() throws MalformedURLException {
        givenAMockedTelemetryClient();
        givenAStartedForwarderInstanceWithCustomPerBatchConfiguration();
        IntStream.range(0, 20).parallel().forEach(ignored -> whenAppendingALog(LOG));
        thenSendBatchFromTelemetryClientShouldBeCalled(2);
    }

    @Test
    @Timeout(3)
    void shouldInitializeTelemetryClientUsingConfigurationLicenseAndEndpoint() throws MalformedURLException {
        givenAMockedTelemetryClient();
        givenAForwarderInstanceWithCustomLicenseAndEndpointConfiguration();
    }

    private void givenAMockedTelemetryClient() {
        telemetryClient = Mockito.mock(TelemetryClient.class);
    }

    private void givenAForwarderInstanceWithCustomLicenseAndEndpointConfiguration() throws MalformedURLException {
        LogForwarderConfiguration configuration =
                LogForwarderConfiguration.builder()
                        .setEndpoint("https://custom-endpoint/log/v1")
                        .setLicense("a-custom-license")
                        .build();
        forwarder = Mockito.spy(new LogForwarder("testing-library", configuration));
    }

    private void givenAStartedForwarderInstanceWithDefaultConfiguration() {
        LogForwarderConfiguration configuration = LogForwarderConfiguration.builder().build();
        forwarder = Mockito.spy(new LogForwarder("testing-library", configuration));
        Mockito.doReturn(telemetryClient).when(forwarder).createTelemetryClient(any());
        forwarder.start();
    }

    private void givenAStartedForwarderInstanceWithCustomPerBatchConfiguration() {
        LogForwarderConfiguration configuration = LogForwarderConfiguration.builder()
                .setMaxLogsPerBatch(10)
                .build();
        forwarder = Mockito.spy(new LogForwarder("testing-library", configuration));
        Mockito.doReturn(telemetryClient).when(forwarder).createTelemetryClient(any());
        forwarder.start();
    }

    private void whenAppendingALog(Log log) {
        forwarder.append(log);
    }

    private void thenSentBatchShouldContainTheLog(Log log) {
        assertEquals(1, sentLogBatch.size());
        assertTrue(sentLogBatch.getTelemetry().contains(log));
    }

    private void thenSendBatchFromTelemetryClientShouldBeCalled(int atLeast) {
        ArgumentCaptor<LogBatch> capturedBatch = ArgumentCaptor.forClass(LogBatch.class);
        Mockito.verify(telemetryClient, Mockito.timeout(FIVE_SECONDS).atLeast(atLeast)).sendBatch(capturedBatch.capture());
        sentLogBatch = capturedBatch.getValue();
    }

    private void mockAgent() {
        // Agent mock
        Config config = Mockito.mock(Config.class);
        Agent agent = Mockito.mock(Agent.class);
        Mockito.when(config.getValue(eq("license_key"))).thenReturn("a-sample-license-key");
        Mockito.when(agent.getConfig()).thenReturn(config);

        // Agent mocked attributes
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("some.agent.key", "an-agent-value");
        Mockito.when(agent.getLinkingMetadata()).thenReturn(attributesMap);

        // Replace agent supplier
        LogForwarder.agentSupplier = () -> agent;
    }

    @BeforeEach
    void init() {
        mockAgent();
    }

    @AfterEach
    void tearDown() throws Exception {
        forwarder.shutdown();
    }

}
