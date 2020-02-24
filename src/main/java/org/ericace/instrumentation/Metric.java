package org.ericace.instrumentation;

/**
 * Defines a metric. Metrics can either be incremented, or have a value set.
 */
public interface Metric {
    /**
     * Increments the metric value
     */
    void incValue();

    /**
     * Sets the metric value to the passed value
     *
     * @param amt the value to set the metric to
     */
    void setValue(float amt);
}
