package org.ericace.instrumentation;

import io.prometheus.client.*;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps the Prometheus Java client libraries, thus implementing Prometheus instrumentation. See:
 * https://github.com/prometheus/client_java
 */
public class PrometheusInstrumentation extends Instrumentation {

    /**
     * Defines the port that will expose the n-body metrics to Prometheus. Can be manually inspected e.g. using
     * curl: watch -n 1 curl -s localhost:12345/metrics
     */
    private static final int PROMETHEUS_EXPORTER_PORT = 12345;

    /**
     * The Prometheus-provided HTTP server that serves the metrics collected by the application
     */
    private final HTTPServer httpServer;

    /**
     * Prometheus Java client doesn't allow querying the registry so we maintain a list of registered metrics
     * here, because attempting to register the same metric name twice in the Prometheus Java client throws.
     */
    private final Map<String, Collector> collectors = new HashMap<>();

    /**
     * Initializes the Prometheus client libs to collect JVM metrics, and starts an HTTP server to
     * serve the metrics to Prometheus
     */
    PrometheusInstrumentation() {
        super();
        try {
            // Export JVM metrics too
            DefaultExports.initialize();
            httpServer = new HTTPServer(PROMETHEUS_EXPORTER_PORT);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize Prometheus exporter on port " + PROMETHEUS_EXPORTER_PORT);
        }
    }

    /**
     * Stops the Prometheus HTTP server
     */
    @Override
    public void stop() {
        httpServer.stop();
    }

    @Override
    public Metric register(String name, String label, String help, MetricType metricType) {
        if ((label != null && !name.contains("/")) || (label == null && name.contains("/"))) {
            throw new IllegalArgumentException("Metric name form is incompatible with label value");
        }
        if (metricType == MetricType.SUMMARY) {
            return buildSummaryMetric(name, label, help);
        } else if (metricType == MetricType.GAUGE) {
            return buildGaugeMetric(name, label, help);
        } else {
            return buildCounterMetric(name, label, help);
        }
    }

    /**
     * Builds a "counter" metric. A counter metric only ever increases, so the only valid operation
     * is "increment".
     *
     * @param name  The metric name. Refer to the {@link Instrumentation} class for allowed naming
     * @param label The metric label. Refer to the {@link Instrumentation} class for semantics
     * @param help  A descriptive phrase for the metric to improve comprehension
     *
     * @return the Metric
     */
    private Metric buildCounterMetric(String name, String label, String help) {
        if (collectors.containsKey(name)) {
            return new PrometheusMetric(collectors.get(name), MetricType.COUNT, label);
        }
        return new PrometheusMetric(buildMetric(Counter.build(), name, help), MetricType.COUNT, label);
    }

    /**
     * Builds a "summary" metric. A summary metric is additive. Every time the value is set, the metric
     * total increases.
     *
     * @param name  The metric name. Refer to the {@link Instrumentation} class for allowed naming
     * @param label The metric label. Refer to the {@link Instrumentation} class for semantics
     * @param help  A descriptive phrase for the metric to improve comprehension
     *
     * @return the Metric
     */
    private Metric buildSummaryMetric(String name, String label, String help) {
        if (collectors.containsKey(name)) {
            return new PrometheusMetric(collectors.get(name), MetricType.SUMMARY, label);
        }
        return new PrometheusMetric(buildMetric(Summary.build(), name, help), MetricType.SUMMARY, label);
    }

    /**
     * Builds a "gauge" metric. A gauge metric goes up and down
     *
     * @param name  The metric name. Refer to the {@link Instrumentation} class for allowed naming
     * @param label The metric label. Refer to the {@link Instrumentation} class for semantics
     * @param help  A descriptive phrase for the metric to improve comprehension
     *
     * @return the Metric
     */
    private Metric buildGaugeMetric(String name, String label, String help) {
        if (collectors.containsKey(name)) {
            return new PrometheusMetric(collectors.get(name), MetricType.GAUGE, label);
        }
        return new PrometheusMetric(buildMetric(Gauge.build(), name, help), MetricType.GAUGE, label);
    }

    /**
     * Builds the Prometheus metric that is wrapped by the class
     *
     * @param b     A Prometheus collector builder
     * @param name  The metric name. Refer to the {@link Instrumentation} class for allowed naming
     * @param help  A descriptive phrase for the metric to improve comprehension
     *
     * @return A Prometheus collector instance
     */
    private Collector buildMetric(SimpleCollector.Builder b, String name, String help) {
        String [] nameElements = name.split("/");
        b.name(nameElements[0]);
        b.help(help);
        if (nameElements.length == 2) {
            b.labelNames(nameElements[1]);
        }
        Collector c = b.register();
        collectors.put(name, c);
        return c;
    }

    /**
     * Provides the Prometheus-specific metric implementation for one Metric
     */
    static class PrometheusMetric implements Metric {
        /**
         * The Prometheus {@code Collector} wrapped by the metric
         */
        private final Collector collector;

        /**
         * The metric type - maps exactly to Prometheus metric types
         */
        private final MetricType metricType;

        /**
         * The metric label. Refer to the {@link Instrumentation} class for semantics
         */
        private final String label;

        /**
         * Creates the instance from the passed params
         *
         * @param collector  The Prometheus {@code Collector} wrapped by the metric
         * @param metricType maps exactly to Prometheus metric types
         * @param label      Refer to the {@link Instrumentation} class for semantics
         */
        PrometheusMetric(Collector collector, MetricType metricType, String label) {
            this.collector = collector;
            this.metricType = metricType;
            this.label = label;
        }

        /**
         * Increments a metric value if the metric is a counter metric.
         *
         * @throws RuntimeException if the metric is not a counter
         */
        @Override
        public void incValue() {
            if (metricType == MetricType.COUNT) {
                if (label == null) {
                    ((Counter) collector).inc();
                } else {
                    ((Counter) collector).labels(label).inc();
                }
            } else {
                throw new RuntimeException("Incorrect metric type");
            }
        }

        /**
         * Sets the metric value if the metric is a summary metric or a gauge metric. For a summary metric,
         * the Prometheus Java code adds the passed value to the accumulating summary. For a gauge metric
         * the gauge value is set from the passed value directly.
         *
         * @throws RuntimeException if the metric is not a summary metric or a gauge metric
         */
        @Override
        public void setValue(float amt) {
            if (metricType == MetricType.SUMMARY) {
                if (label == null) {
                    ((Summary) collector).observe(amt);
                } else {
                    ((Summary) collector).labels(label).observe(amt);
                }
            } else if (metricType == MetricType.GAUGE) {
                if (label == null) {
                    ((Gauge) collector).set(amt);
                } else {
                    ((Gauge) collector).labels(label).set(amt);
                }
            } else {
                throw new RuntimeException("Incorrect metric type");
            }
        }
    }
}
