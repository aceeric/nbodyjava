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
     *
     * @return a vector as as described
     */
    @SuppressWarnings("unused")
    public static SimpleVector getVectorEven(SimpleVector center, double radius) {
        double d, x, y, z;
        do {
            x = Math.random() * 2.0 - 1.0;
            y = Math.random() * 2.0 - 1.0;
            z = Math.random() * 2.0 - 1.0;
            d = x*x + y*y + z*z;
        } while (d > 1.0);
        return new SimpleVector((float) ((x * radius) + center.x), (float) ((y * radius) + center.y),
                (float) ((z * radius) + center.z));
    }

    /**
     * Similar to {@link #getVectorEven} except is more likely to return a vector closer toward the center of the
     * sphere. This function is based on:
     *
     * https://karthikkaranth.me/blog/generating-random-points-in-a-sphere/
     *
     * @param center The Vector around which to center the generated vector
     *
     * @return a vector as as described
     */
    @SuppressWarnings("unused")
    public static SimpleVector getVectorConcentrated(SimpleVector center) {
        double x = Math.random() - 0.5;
        double y = Math.random() - 0.5;
        double z = Math.random() - 0.5;
        double mag = Math.sqrt(x * x + y * y + z * z);
        x /= mag;
        y /= mag;
        z /= mag;
        double d = Math.random() * 200;
        return new SimpleVector((float) ((x * d) + center.x), (float) ((y * d) + center.y), (float) ((z * d) + center.z));
    }

    /**
     * Generates a vector located on the surface of a virtual sphere centered at the passed point, having the
     * passed radius. Based on:
     * https://math.stackexchange.com/questions/1585975/how-to-generate-random-points-on-a-sphere/1586185#1586185
     *
     * @param center The center of the sphere
     * @param radius The radius
     *
     * @return a randomly-generated point on the surface of the sphere
     */
    @SuppressWarnings("unused")
    public static SimpleVector getVectorOnSphere(SimpleVector center, double radius) {
        Random r = new Random();
        double x = r.nextGaussian();
        double y = r.nextGaussian();
        double z = r.nextGaussian();
        // normalize
        x *= 1 / Math.sqrt((x*x) + (y*y) + (z*z));
        y *= 1 / Math.sqrt((x*x) + (y*y) + (z*z));
        z *= 1 / Math.sqrt((x*x) + (y*y) + (z*z));
        x *= radius;
        y *= radius;
        z *= radius;
        return new SimpleVector((float) (center.x + x), (float) (center.y + y), (float) (center.z + z));
    }
}
