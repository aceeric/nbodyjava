package org.ericace.instrumentation;

/**
 * Defines the interface for supported instrumentation implementations. In all cases, when a metric
 * is registered, the metric name is accepted in one of two forms:
 * <ol>
 *     <li>"metric-name" - in this case, the metric is a single unique metric with no sub-metrics</li>
 *     <li>"metric-name/category" - in this case, the metric supports sub-categorization. An example would
 *         be something like "cpu_cycles/thread". In this example, if the metric were a counter, the label value
 *         for the "thread" category would be specified when the metric is registered. E.g.:
 *         {@code .registerSummary("cpu_cycles/thread", "this_thread_name");}. Then, another thread could call
 *         {@code .registerSummary("cpu_cycles/thread", "other_thread_name");}. In this scenario, metrics can
 *         be accumulated and reported for each thread separately. Conceptually: "show me cpu_cycles where thread==
 *         this_thread_name". Or, metrics could be reported rolled up: "show me cpu_cycles". The precise query syntax
 *         depends on the metrics implementation. For Prometheus, the intent is to support a query like:
 *         {@code cpu_cycles{thread="this_thread_name"}}
 *         </li>
 * </ol>
 */
public abstract class Instrumentation {
    /**
     * If the instrumentation implementation has a thread that supports an external entity's ability to gather
     * metrics from the app via some RPC mechanism (e.g. an HTTP endpoint) then this interface method is provided
     * to allow the instrumentation consumer to stop that thread.
     */
    public void stop() {};

    /**
     * Creates and registers a <i>counter</i> metric. A counter is a monotonically increasing value.
     *
     * @param name  The counter name, and optional category. E.g.: "foo", or "foo/method"
     * @param label A label to allow multiple metric values with the same name to be distinguished.
     *              E.g. "thread-1-", "thread-2". Only valid if name is in the form "name/category"
     *
     * @return the metric instance
     */
    public Metric registerCounter(String name, String label) {
        return register(name, label, MetricType.COUNT);
    }

    /**
     * Creates an registers a counter metric without a label.
     * @see #registerCounter(String, String)
     */
    public Metric registerCounter(String name) {
        return register(name, null, MetricType.COUNT);
    }

    /**
     * Creates and registers a <i>gauge</i> metric. A gauge is like a counter, but can go up and down
     *
     * @param name  The gauge name, and optional category. E.g.: "foo", or "foo/method"
     * @param label A label to allow multiple gauge values with the same name to be distinguished.
     *              E.g. "thread-1-", "thread-2". Only valid if name is in the form "name/category"
     *
     * @return the gauge instance
     */
    public Metric registerGauge(String name, String label) {
        return register(name, label, MetricType.GAUGE);
    }

    /**
     * Creates an registers a gauge metric without a label.
     * @see #registerGauge(String) (String, String)
     */
    public Metric registerGauge(String name) {
        return register(name, null, MetricType.GAUGE);
    }

    /**
     * Creates and registers a <i>summary</i> metric. A summary metric has a value that fluctuates. The distinction
     * between a summary and a gauge is vague. Generally, it seems that summaries might express values with large
     * fluctuations, e.g., HTTP response size in bytes.
     *
     * @param name  The summary name, and optional category. E.g.: "foo", or "foo/method"
     * @param label A label to allow multiple summary values with the same name to be distinguished.
     *              E.g. "thread-1-", "thread-2". Only valid if name is in the form "name/category"
     *
     * @return the summary instance
     */
    public Metric registerSummary(String name, String label) {
        return register(name, label, MetricType.SUMMARY);
    }

    /**
     * Creates an registers a summary metric without a label.
     * @see #registerSummary(String, String)
     */
    public Metric registerSummary(String name) {
        return register(name, null, MetricType.SUMMARY);
    }

    /**
     * Implementation-specific metric creation and registration
     *
     * @param name       The metric name, and optional category. E.g.: "foo", or "foo/method"
     * @param label      A label to allow multiple summary values with the same name to be distinguished.
     *                   E.g. "thread-1-", "thread-2". Only valid if name is in the form "name/category"
     * @param metricType The metric type
     *
     * @return the metric instance
     */
    abstract Metric register(String name, String label, MetricType metricType);
}
