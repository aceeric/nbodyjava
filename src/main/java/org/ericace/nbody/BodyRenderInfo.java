package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A value class that holds the computed position of a body in the simulation, and other elements
 * needed by the graphics engine. The values are all copies from a Body instance. The ID matches the
 * ID of the Body from which it was created. For simplicity the fields are publicly accessible rather
 * than being wrapped in getters.
 */
class BodyRenderInfo {
    private static final Logger logger = LogManager.getLogger(BodyRenderInfo.class);

    final int id;
    final boolean exists;
    final double radius;
    final double x, y, z;
    final boolean isSun;

    /**
     * Creates an instance representing a body that exists
     *
     * @param id     The id of the body. It is used to find the corresponding JME object by the rendering code
     * @param x      x position
     * @param y      y "
     * @param z      z "
     * @param radius radius
     * @param isSun  true if this is a sun (the rendering engine should create a light source for it)
     */
    BodyRenderInfo(int id, double x, double y, double z, double radius, boolean isSun) {
        logger.info("New existent BodyRenderInfo ID={}", id);
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        exists = true;
        this.isSun = isSun;
    }

    /**
     * Creates an instance representing a body that no longer exists. This instructs the
     * rendering thread to remove the object from the scene. The computation loop will
     * subsequently remove it from the body queue.
     *
     * @param id The id of the body
     */
    BodyRenderInfo(int id) {
        logger.info("New non-existent BodyRenderInfo ID={}", id);
        this.id = id;
        exists = isSun = false;
        radius = x = y = z = 0;
    }

    @Override
    public String toString() {
        return String.format("id: %d; x: %f; y: %f; z: %f; radius: %f", id, x, y, z, radius);
    }
}
