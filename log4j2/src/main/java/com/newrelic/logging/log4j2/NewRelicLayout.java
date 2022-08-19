/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.logging.log4j2;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.newrelic.logging.core.ElementName;
import com.newrelic.logging.core.ExceptionUtil;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A {@link Layout} that writes the New Relic JSON format.
 *
 * This layout must be added to an {@link AbstractAppender} via {@link AbstractAppender.Builder#withLayout(Layout)} or XML.
 * The New Relic layout has specific fields and field names and has no customizable elements. To configure,
 * update your logging config xml like this:
 *
 * <pre>{@code
 *     <File name="MyFile" fileName="logs/app-log-file.log">
 *         <NewRelicLayout/>
 *     </File>
 * }</pre>
 *
 * @see <a href="https://logging.apache.org/log4j/2.x/manual/appenders.html#FileAppender">The FileAppender, for example</a>
 */
@Plugin(name = NewRelicLayout.PLUGIN_NAME, category = "Core", elementType = Layout.ELEMENT_TYPE)
public class NewRelicLayout extends AbstractStringLayout {
    static final String PLUGIN_NAME = "NewRelicLayout";

    @PluginFactory
    public static NewRelicLayout factory() {
        return new NewRelicLayout(StandardCharsets.UTF_8);
    }

    private NewRelicLayout(Charset charset) {
        super(charset);
    }

    @Override
    public String toSerializable(LogEvent event) {
        StringWriter sw = new StringWriter();

        try (JsonGenerator generator = new JsonFactory().createGenerator(sw)) {
            writeToGenerator(event, generator);
        } catch (IOException e) {
            return e.toString();
        }

        return sw.toString() + "\n";
    }

    private void writeToGenerator(LogEvent event, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeObjectField(ElementName.MESSAGE, event.getMessage().getFormattedMessage());
        generator.writeObjectField(ElementName.TIMESTAMP, event.getTimeMillis());
        generator.writeObjectField(ElementName.THREAD_NAME, event.getThreadName());
        generator.writeObjectField(ElementName.LOG_LEVEL, event.getLevel().toString());
        generator.writeObjectField(ElementName.LOGGER_NAME, event.getLoggerName());

        if (event.isIncludeLocation() && event.getSource() != null) {
            generator.writeObjectField(ElementName.CLASS_NAME, event.getSource().getClassName());
            generator.writeObjectField(ElementName.METHOD_NAME, event.getSource().getMethodName());
            generator.writeObjectField(ElementName.LINE_NUMBER, event.getSource().getLineNumber());
        }

        Map<String, String> map = event.getContextData().toMap();
        if (map != null) {

            Map<String, Set<String>> multiValueMap = new HashMap<>();

            for (Map.Entry<String, String> entry : map.entrySet()) {

                String key = entry.getKey().startsWith(NewRelicContextDataProvider.NEW_RELIC_PREFIX)
                        ? entry.getKey().substring(NewRelicContextDataProvider.NEW_RELIC_PREFIX.length())
                        : entry.getKey();

                multiValueMap.computeIfAbsent(key, k -> new HashSet<>())
                             .add(entry.getValue());
            }

            for (Map.Entry<String, Set<String>> entry : multiValueMap.entrySet()) {

                String[] values = entry.getValue().toArray(new String[0]);

                if (values.length > 1) {

                    generator.writeArrayFieldStart(entry.getKey());
                    generator.writeArray(values, 0, values.length);
                    generator.writeEndArray();

                } else {

                    generator.writeStringField(entry.getKey(), values[0]);
                }
            }
        }

        Throwable throwable = event.getThrown();
        if (throwable != null) {
            generator.writeObjectField(ElementName.ERROR_CLASS, throwable.getClass().getName());
            generator.writeObjectField(ElementName.ERROR_MESSAGE, throwable.getMessage());
            generator.writeObjectField(ElementName.ERROR_STACK, ExceptionUtil.getErrorStack(throwable));
        }

        generator.writeEndObject();
    }
}
