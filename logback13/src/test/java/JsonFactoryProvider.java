import com.fasterxml.jackson.core.JsonFactory;

public class JsonFactoryProvider {
    private static final JsonFactory jsonFactory = new JsonFactory();

    public static JsonFactory getInstance() {
        return jsonFactory;
    }

    private JsonFactoryProvider() {

    }
}
