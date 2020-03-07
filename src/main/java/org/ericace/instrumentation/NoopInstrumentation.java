package org.ericace.instrumentation;

/**
 * An instrumentation implementation that does nothing. This is the default implementation of the
 * instrumentation package. The objective is to enable package users to include instrumentation code in
 * their classes, but not incur too much overhead to run without instrumentation.
 */
public class NoopInstrumentation extends Instrumentation {
    /**
     * A singleton metric that does nothing
     */
    private static final Metric noOpMetric = new Metric() {
        @Override
        public void incValue() {}

        @Override
        public void setValue(float amt) {}
    };

    /**
     * Returns the singleton nop metric
     *
     * @param name       The metric name, and optional category. E.g.: "foo", or "foo/method"
     * @param label      A label to allow multiple summary values with the same name to be distinguished.
     *                   E.g. "thread-1-", "thread-2". Only valid if name is in the form "name/category"
     * @param metricType The metric type
     *
     * @return the singleton nop metric
     */
    @Override
    public Metric register(String name, String label, String help, MetricType metricType) {return noOpMetric;}
}
