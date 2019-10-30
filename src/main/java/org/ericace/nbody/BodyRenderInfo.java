package org.ericace.nbody;

/**
 * A value class that holds the computed position of a body in the simulation, and other elements
 * needed by the graphics engine. The value are all copies of a Body instance. The ID matches the
 * ID of the Body from which it was created.
 */
class BodyRenderInfo {
    final int id;
    final boolean exists;
    final double radius;
    final double x, y, z;
    boolean sun;

    /**
     * Creates an instance representing a body that exists
     *
     * @param id     The id of the body. It is used to find the corresponding JME object by the rendering code
     * @param x      x position
     * @param y      y "
     * @param z      z "
     * @param radius radius
     */
    BodyRenderInfo(int id, double x, double y, double z, double radius) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        exists = true;
        sun = false;
    }

    /**
     * Creates an instance representing a body that no longer exists. This instructs the
     * rendering thread to remove the object from the scene. The computation loop will
     * subsequently remove it from the body queue.
     *
     * @param id The id of the body
     */
    BodyRenderInfo(int id) {
        this.id = id;
        exists = false;
        radius = x = y = z = 0;
    }

    /**
     * It may be desirable for the simulation to have a sun. If so, a body can be marked as the sun
     * and then the Sphere created for that body in the {@link JMEApp} class will be rendered
     * accordingly
     */
    void setSun() {
        sun = true;
    }

    @Override
    public String toString() {
        return String.format("id: %d; x: %f; y: %f; z: %f; radius: %f", id, x, y, z, radius);
    }
}
