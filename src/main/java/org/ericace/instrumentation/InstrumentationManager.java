package org.ericace.instrumentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class InstrumentationManager {

    /**
     * Defines a java property that can be provided to the JVM to control the instrumentation class that
     * is used to implement instrumentation. E.g.:
     * <p>
     * -Dorg.ericace.instrumentation.class=org.ericace.instrumentation.PrometheusInstrumentation
     * </p>
     * If not provided to the JVM, then an instance of the {@link NoopInstrumentation} class is used,
     * which, as the name implies, performs no instrumentation.
     */
    private static final String INSTRUMENTATION_CLASS_PROPERTY = "org.ericace.instrumentation.class";
    private static Instrumentation instance;

    /**
     * @return a reference to the singleton instance of the class being used to implement instrumentation. (Creates
     * the  instance on the first call.)
     */
    public static Instrumentation getInstrumentation() {
        if (instance == null) {
            String instrumentationClassName = null;
            try {
                instrumentationClassName = System.getProperty(INSTRUMENTATION_CLASS_PROPERTY);
                instance = getInstrumentation(instrumentationClassName);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Cannot instantiate metrics class: " + instrumentationClassName);
            }
        }
        return instance;
    }

    /**
     * Creates - and returns an instance of - an instrumentation class
     *
     * @param className The full package/name of an instrumentation class. The specified class must extend
     *                  the {@link Instrumentation} abstract class
     *
     * @return the instance
     */
    private static Instrumentation getInstrumentation(String className) throws NoSuchMethodException,
            InstantiationException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        if (className == null) {
            return new NoopInstrumentation();
        } else {
            Class<? extends Instrumentation> clazz = Class.forName(className).asSubclass(Instrumentation.class);
            Constructor<? extends Instrumentation> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        }
    }
}
