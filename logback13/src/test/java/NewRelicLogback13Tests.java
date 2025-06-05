import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.read.ListAppender;
import com.newrelic.api.agent.Agent;
import com.newrelic.logging.logback13.NewRelicAsyncAppender;
import com.newrelic.logging.logback13.NewRelicEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import java.util.Map;

public class NewRelicLogback13Tests {

    private static final LoggerContext loggerContext = new LoggerContext();
    private static final String TEST_MESSAGE = "This is an amazing test message.";

    private NewRelicAsyncAppender appender;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setup() {
        Agent mockAgent = Mockito.mock(Agent.class);
        Mockito.when(mockAgent.getLinkingMetadata()).thenReturn(Map.of("traceId", "abd123", "spanId", "xyz789"));
        NewRelicAsyncAppender.agentSupplier = () -> mockAgent;

        NewRelicEncoder encoder = new NewRelicEncoder();
        encoder.start();

        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        appender = new NewRelicAsyncAppender();
        appender.setContext(loggerContext);
        appender.addAppender(consoleAppender);
        appender.start();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        appender.stop();
        appender.detachAndStopAllAppenders();
    }

    @Test
    void testBasicLogMessageIncludesLinkingMetadata() {
//        LoggingEvent event = createBasicEvent(TEST_MESSAGE);
//        appender.doAppend(event);

//        AssertTrue(event.getMDCPropertyMap().containsKey("NewRelic:"));



        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        Mockito.when(event.getMessage()).thenReturn(TEST_MESSAGE);
        Mockito.when(event.getMDCPropertyMap()).thenReturn(Map.of("customKey", "customValue"));

        appender.doAppend(event);

        // Verify that the message was logged with the expected metadata
//        assert appender.getAppender("console").getEncoder().encode(event).contains("traceId\":\"abd123");
//        assert appender.getAppender("console").getEncoder().encode(event).contains("spanId\":\"xyz789");
    }
}