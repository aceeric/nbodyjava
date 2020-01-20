package org.ericace.nbody;

/**
 * A simple value class. Rather than pulling the JME <i>Vector3f</i> class
 * into this package, define our own vector class. Reduces coupling between the
 * simulation and the rendering engine. TODO double instead of float?
 */
public class SimpleVector {
    /**
     * Public because this is just a value class and has no functionality
     */
    public float x, y, z;

    /**
     * Convenience constructor
     */
    public SimpleVector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
