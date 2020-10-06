package com.newrelic.logging.logback;

import com.fasterxml.jackson.core.JsonFactory;

public class JsonFactoryProvider {
    private static final JsonFactory jsonFactory = new JsonFactory();

    static JsonFactory getInstance() {
        return jsonFactory;
    }

    private JsonFactoryProvider() {

    }
}
