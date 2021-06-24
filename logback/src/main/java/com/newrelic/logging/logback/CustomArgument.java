package com.newrelic.logging.logback;

public class CustomArgument {
    private final String key;
    private final String value;

    public CustomArgument(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static CustomArgument keyValue(String key, String value) {
        return new CustomArgument(key, value);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
