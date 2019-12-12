package org.ericace.instrumentation;

import io.prometheus.client.*;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps the Prometheus Java client libraries, thus implementing Prometheus instrumentation
 */
public class PrometheusInstrumentation extends Instrumentation {

    private static final int PROMETHEUS_EXPORTER_PORT = 12345;
    private final HTTPServer httpServer;
    /**
     * Prometheus Java client doesn't allow querying the registry so we maintain a list of registered metrics
     * here, because attempting to register the same metric name twice in the Prometheus Java client throws.
     */
    private final Map<String, Collector> collectors = new HashMap<>();

    PrometheusInstrumentation() {
        super();
        try {
            // Export JVM metrics
            DefaultExports.initialize();
            // to test: watch -n 1 curl -s localhost:12345/metrics
            httpServer = new HTTPServer(PROMETHEUS_EXPORTER_PORT);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize Prometheus exporter on port " + PROMETHEUS_EXPORTER_PORT);
        }
    }

    @Override
    public void stop() {
        httpServer.stop();
    }

    @Override
    public Metric register(String name, String label, MetricType metricType) {
        if ((label != null && !name.contains("/")) || (label == null && name.contains("/"))) {
            throw new IllegalArgumentException("Metric name form is incompatible with label value");
        }
        if (metricType == MetricType.SUMMARY) {
            return buildSummaryMetric(name, label);
        } else if (metricType == MetricType.GAUGE) {
            return buildGaugeMetric(name, label);
        } else {
            return buildCounterMetric(name, label);
        }
    }

    private Metric buildCounterMetric(String name, String label) {
        if (collectors.containsKey(name)) {
            return new PrometheusMetric(collectors.get(name), MetricType.COUNT, label);
        }
        return new PrometheusMetric(buildMetric(Counter.build(), name), MetricType.COUNT, label);
    }

    private Metric buildSummaryMetric(String name, String label) {
        if (collectors.containsKey(name)) {
            return new PrometheusMetric(collectors.get(name), MetricType.SUMMARY, label);
        }
        return new PrometheusMetric(buildMetric(Summary.build(), name), MetricType.SUMMARY, label);
    }

    private Metric buildGaugeMetric(String name, String label) {
        if (collectors.containsKey(name)) {
            return new PrometheusMetric(collectors.get(name), MetricType.GAUGE, label);
        }
        return new PrometheusMetric(buildMetric(Gauge.build(), name), MetricType.GAUGE, label);
    }

    private Collector buildMetric(SimpleCollector.Builder b, String name) {
        String [] nameElements = name.split("/");
        b.name(nameElements[0]);
        b.help("future");
        if (nameElements.length == 2) {
            b.labelNames(nameElements[1]);
        }
        Collector c = b.register();
        collectors.put(name, c);
        return c;
    }

    static class PrometheusMetric implements Metric {
        private final Collector collector;
        private final MetricType metricType;
        private final String label;

        PrometheusMetric(Collector collector, MetricType metricType, String label) {
            this.collector = collector;
            this.metricType = metricType;
            this.label = label;
        }

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

        @Override
        public void setValue(double amt) {
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
