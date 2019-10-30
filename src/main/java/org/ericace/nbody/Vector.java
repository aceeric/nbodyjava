package org.ericace.nbody;

/**
 * A simple value class. Rather than pulling the JME <i>Vector3f</i> class
 * into this package, define our own vector class. This way it should be
 * easier to try the simulation with a different rendering engine.
 */
class Vector {
    /**
     * Public because this is just a value class and has no functionality
     */
    float x, y, z;

    /**
     * Convenience constructor
     */
    Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}