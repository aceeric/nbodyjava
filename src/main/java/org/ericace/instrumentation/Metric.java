package org.ericace.instrumentation;

public interface Metric {
    void incValue();            // for counter
    void setValue(double amt);  // for Summary and Gauge
}
