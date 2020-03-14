package org.ericace.nbody;

import java.util.Random;

/**
 * A simple value class. Rather than pulling the JME <i>Vector3f</i> class
 * into this package, define our own vector class. Reduces coupling between the
 * simulation and the rendering engine. Also includes some utility methods.
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

    /**
     * Generates a vector that is evenly distributed within a virtual sphere around the vector defined
     * in the passed param. Meaning - if called multiple times, the result will be a set of vectors
     * evenly distributed within a sphere. This function is based on:
     *
     * https://karthikkaranth.me/blog/generating-random-points-in-a-sphere/
     *
     * @param center The Vector around which to center the generated vector
     * @param radius The radius within which to generate the vector
     *
     * @return a vector as as described
     */
    @SuppressWarnings("unused")
    public static SimpleVector getVectorEven(SimpleVector center, float radius) {
        float d, x, y, z;
        do {
            x = (float) Math.random() * 2F - 1F;
            y = (float) Math.random() * 2F - 1F;
            z = (float) Math.random() * 2F - 1F;
            d = x*x + y*y + z*z;
        } while (d > 1.0);
        return new SimpleVector(((x * radius) + center.x), ((y * radius) + center.y), ((z * radius) + center.z));
    }

    /**
     * Similar to {@link #getVectorEven} except is more likely to return a vector closer toward the center of the
     * sphere. This function is based on:
     *
     * https://karthikkaranth.me/blog/generating-random-points-in-a-sphere/
     *
     * @param center The Vector around which to center the generated vector
     * @param radius The radius within which to generate the vector
     *
     * @return a vector as as described
     */
    @SuppressWarnings("unused")
    public static SimpleVector getVectorConcentrated(SimpleVector center, float radius) {
        float x = (float) Math.random() - 0.5F;
        float y = (float) Math.random() - 0.5F;
        float z = (float) Math.random() - 0.5F;
        float mag = (float) Math.sqrt(x * x + y * y + z * z);
        x /= mag;
        y /= mag;
        z /= mag;
        float d = (float) Math.random() * radius;
        return new SimpleVector(((x * d) + center.x), ((y * d) + center.y), ((z * d) + center.z));
    }

    /**
     * Generates a vector located on the surface of a virtual sphere centered at the passed point, having the
     * passed radius. Based on:
     *
     * https://math.stackexchange.com/questions/1585975/how-to-generate-random-points-on-a-sphere/1586185#1586185
     *
     * @param center The center of the sphere
     * @param radius The radius within which to generate the vector
     *
     * @return a randomly-generated point on the surface of the sphere
     */
    @SuppressWarnings("unused")
    public static SimpleVector getVectorOnSphere(SimpleVector center, float radius) {
        Random r = new Random();
        float x = (float) r.nextGaussian();
        float y = (float) r.nextGaussian();
        float z = (float) r.nextGaussian();
        // normalize
        x *= 1 / Math.sqrt((x*x) + (y*y) + (z*z));
        y *= 1 / Math.sqrt((x*x) + (y*y) + (z*z));
        z *= 1 / Math.sqrt((x*x) + (y*y) + (z*z));
        x *= radius;
        y *= radius;
        z *= radius;
        return new SimpleVector(center.x + x, center.y + y, center.z + z);
    }
}
