package com.newrelic.logging.jul;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class ListHandler extends Handler {
    final List<String> capturedLogs = new LinkedList<>();
    final CountDownLatch latch;
    static volatile ListHandler lastInstance = null;

    public ListHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (lastInstance != null) {
            throw new IllegalStateException("Cannot create multiple instances of this handler!");
        }

        int expectedMessageCount = Integer.parseInt(LogManager.getLogManager().getProperty(ListHandler.class.getName() + ".expectedCount"));
        latch = new CountDownLatch(expectedMessageCount);

        LogManager manager = LogManager.getLogManager();
        String formatterName = manager.getProperty(getClass().getName() + ".formatter");

        Formatter formatter = (Formatter) Class.forName(formatterName).newInstance();
        setFormatter(formatter);

        if (getFormatter() == null) {
            throw new NullPointerException("The formatter was not set.");
        }

        lastInstance = this;
    }

    @Override
    public void publish(LogRecord record) {
        capturedLogs.add(getFormatter().format(record));
        latch.countDown();
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {
    }
}
