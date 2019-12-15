/**
 * Provides a wrapper around instrumentation. The primary interface to the package is the
 * {@link org.ericace.instrumentation.InstrumentationManager} class. This class instantiates an instrumentation
 * singleton which enables the caller to register metrics. Once a metric is registered, the caller can
 * instrument their code by accessing the various metric methods.
 * <p>
 * By default, a <i>NOP</i> instrumentation class is instantiated - which minimizes
 * overhead of including instrumentation in the n-body code. The noop instrumentation does
 * nothing. If a JVM property is provided specifying the instrumentation class to use, then that class
 * is instantiated. Presently, only Prometheus instrumentation is included.
 * </p>
 * The following classes comprise the package:
 * <ol>
 * <li>{@link org.ericace.instrumentation.Instrumentation} - Defines an interface that a specific instrumentation
 * implementation must implement</li>
 * <li>{@link org.ericace.instrumentation.InstrumentationManager} - Instantiates a NOOP instrumentation class, unless
 * a JVM property is defined specifying a different instrumentation implementation class</li>
 * <li>{@link org.ericace.instrumentation.Metric} - Defines the operators allowed on an individual instrumentation
 * metric</li>
 * <li>{@link org.ericace.instrumentation.MetricType} - Defines the supported metric types. These are strongly
 * influenced by Prometheus</li>
 * <li>{@link org.ericace.instrumentation.NoopInstrumentation} - Provides a NOOP instrumentation implementation
 * to minimize runtime overhead when running without instrumentation, while not requiring consumers to remove
 * instrumentation code</li>
 * <li>{@link org.ericace.instrumentation.PrometheusInstrumentation} - Integrates with Prometheus instrumentation</li>
 * </ol>
 */
package org.ericace.instrumentation;