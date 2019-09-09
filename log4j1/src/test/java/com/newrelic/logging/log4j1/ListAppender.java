package com.newrelic.logging.log4j1;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ListAppender extends AppenderSkeleton {
    public final List<String> appendedStrings = new LinkedList<>();
    public final CountDownLatch latch;

    public ListAppender(Layout layout, int expectedCount) {
        latch = new CountDownLatch(expectedCount);
        setLayout(layout);
    }

    @Override
    protected void append(LoggingEvent event) {
        appendedStrings.add(this.layout.format(event));
        latch.countDown();
    }

    @Override
    public void close() { }

    @Override
    public boolean requiresLayout() { return true; }
}
