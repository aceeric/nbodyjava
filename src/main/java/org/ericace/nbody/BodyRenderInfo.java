package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.Float.NaN;

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
    final float radius;
    final float x, y, z;
    final boolean isSun;
    final Body.Color color;

    /**
     * Creates an instance representing a body that exists
     *
     * @param id     The id of the body. It is used to find the corresponding JME object by the rendering code
     * @param x      x position
     * @param y      y "
     * @param z      z "
     * @param radius radius
     * @param isSun  true if this is a sun (the rendering engine should create a light source for it)
     * @param color  body color
     */
    BodyRenderInfo(int id, float x, float y, float z, float radius, boolean isSun, Body.Color color) {
        logger.info("New existent BodyRenderInfo ID={}", id);
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
            logger.info("NaN values. ID={}", id);
        }
        exists = true;
        this.isSun = isSun;
        this.color = color;
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
        this.color = null;
    }

    @Override
    public String toString() {
        return String.format("id: %d; x: %f; y: %f; z: %f; radius: %f", id, x, y, z, radius);
    }
}
