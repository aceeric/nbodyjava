/**
 * Provides a wrapper around instrumentation. The primary interface to the package is the
 * {@link org.ericace.instrumentation.InstrumentationManager} class. This class instantiates an instrumentation
 * singleton which enables the caller to register metrics. Once a metric is registered, the caller can
 * instrument their code by accessing the various metric methods.
 * <p>
 * By default, a <i>NOP</i> instrumentation class is instantiated - which minimizes
 * overhead of including instrumentation in the n-body code. The noop instrumentation does
 * nothing. If a JVM property is provided specifying an instrumentation class to use, then assuming that class
 * is in the classpath, it is instantiated. Presently, only Prometheus instrumentation is included. Refer to
 * {@link org.ericace.instrumentation.PrometheusInstrumentation}
 * </p>
 */
package org.ericace.instrumentation;