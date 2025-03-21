import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import com.newrelic.logging.logback13.NewRelicJsonLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NewRelicJsonLayoutTest {
    private NewRelicJsonLayout layout;
    private LoggerContext mockContext;
    private StatusManager mockStatusManager;

    @BeforeEach
    void setUp() {
        layout = new NewRelicJsonLayout();
        mockContext = mock(LoggerContext.class);
        layout.setContext(mockContext);
        mockStatusManager = mock(StatusManager.class);
        when(mockContext.getStatusManager()).thenReturn(mockStatusManager);
    }

    @Test
    void testContextIsSet() {
        assertNotNull(layout.getContext(), "Context should not be null");
        assertEquals(mockContext, layout.getContext(), "Context should match the one set in the test");
    }

    @Test
    void testLoggingMethodsWithoutNullPointer() {
        layout.addInfo("Test Info Message");
        layout.addWarn("Test Warn Message");
        layout.addError("Test Error Message");

        verify(mockStatusManager, times(3)).add(any(Status.class));
    }
}
