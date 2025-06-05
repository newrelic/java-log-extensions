import com.fasterxml.jackson.core.JsonFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JsonFactoryProviderTest {

    @Test
    void shouldAlwaysGetSameInstance() {
        JsonFactory instance1 = JsonFactoryProvider.getInstance();
        JsonFactory instance2 = JsonFactoryProvider.getInstance();

        assertNotNull(instance1, "Instance 1 should not be null");
        assertNotNull(instance2, "Instance 2 should not be null");
        assertSame(instance1, instance2, "Instances should be the same");
    }

    @Test
    void shouldReturnNonNullJsonFactoryInstance() {
        JsonFactory jsonFactory = JsonFactoryProvider.getInstance();
        assertNotNull(jsonFactory, "JsonFactory instance should not be null");
    }

    @Test
    void shouldReturnSameJsonFactoryInstance() {
        JsonFactory firstInstance = JsonFactoryProvider.getInstance();
        JsonFactory secondInstance = JsonFactoryProvider.getInstance();
        assertSame(firstInstance, secondInstance, "Both instances should be the same");
    }

}
