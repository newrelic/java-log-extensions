package com.newrelic.logging.logback;

import com.fasterxml.jackson.core.JsonFactory;

public class JsonFactoryProvider {
    private static JsonFactory jsonFactory;

    static JsonFactory getInstance() {
        if (jsonFactory == null) {
            jsonFactory = new JsonFactory();
        }
        return jsonFactory;
    }

    private JsonFactoryProvider() {

    }
}
