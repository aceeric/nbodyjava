package org.ericace.instrumentation;

public class NoopInstrumentation extends Instrumentation {
    private static final Metric noOpMetric = new Metric() {
        @Override
        public void incValue() {}

        @Override
        public void setValue(double amt) {}
    };
    @Override
    public Metric register(String name, String label, MetricType metricType) {return noOpMetric;}
}
