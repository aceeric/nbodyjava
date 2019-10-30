package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 */
class Body {
    private static final Logger logger = LogManager.getLogger(Body.class);

    private static final double G = 6.673e-11; // gravitational constant

    /**
     * A unique ID value for each instance
     */
    private final int id;

    /**
     * Radius and mass
     */
    private volatile double radius, mass;

    /**
     * current coordinates of the body
     */
    private double x, y, z;

    /**
     * cumulative velocity
     */
    private double vx, vy, vz;

    /**
     * force on this body from all other bodies - zeroed and re-computed once per sim cycle
     */
    private double fx, fy, fz;

    /**
     * If this body is collapsed into another then this flag is set to false
     * and it will be removed from the simulation
     */
    private volatile boolean exists;

    /**
     * Getter for {@link #exists}
     */
    boolean exists() {
        return exists;
    }

    /**
     * Creates an instance with passed configuration
     *
     * @param id     Every body should be created with a unique ID starting at zero with max <=>= total bodies
     *               -1 because this ID is also used as an array index by the rendering engine. The class
     *               does not enforce this
     * @param x      Position
     * @param y      "
     * @param z      "
     * @param vx     Velocity
     * @param vy     "
     * @param vz     "
     * @param mass   Mass
     * @param radius Radius
     */
    Body(int id, double x, double y, double z, double vx, double vy, double vz, double mass, float radius) {
        exists      = true;
        this.id     = id;
        this.x      = x;
        this.y      = y;
        this.z      = z;
        this.vx     = vx;
        this.vy     = vy;
        this.vz     = vz;
        this.mass   = mass;
        this.radius = radius;
    }

    /**
     * re-computes velocity and position from force accumulated as of the instant of the call. Intended to be
     * called from a single thread - so no two bodies are ever computed concurrently in different
     * threads. Therefore, no concurrency control.
     *
     * @param timeScaling a smoothing factor
     *
     * @return see {@link BodyRenderInfo}
     */
    BodyRenderInfo update(double timeScaling) {
        if (!exists) {
            // creates an instance with exists=false so the graphics engine will remove it from the scene
            return new BodyRenderInfo(id);
        }
        vx += timeScaling * fx / mass;
        vy += timeScaling * fy / mass;
        vz += timeScaling * fz / mass;
        x += timeScaling * vx;
        y += timeScaling * vy;
        z += timeScaling * vz;
        return getRenderInfo();
    }

    /**
     * Returns a {@link BodyRenderInfo} instance with values populated so the body will be rendered
     * by the graphics engine
     *
     * @return see {@link BodyRenderInfo}
     */
    BodyRenderInfo getRenderInfo() {
        return new BodyRenderInfo(id, x, y, z, radius);
    }

    /**
     * A Runnable that the ThreadPoolExecutor can run. Calculates the force on this body from all other
     * bodies. The design is that one thread calculates the force for one instance - therefore the thread
     * can safely write to the force class variables without synchronization code. The only other place the
     * force variables are referenced is in the {@link #update} method which - again - by design runs
     * in a single thread.
     */
    class ForceComputer implements Runnable {
        private final ConcurrentLinkedQueue<Body> bodyQueue;
        private final CountDownLatch latch;
        ForceComputer(ConcurrentLinkedQueue<Body> bodyQueue, CountDownLatch latch) {
            this.bodyQueue = bodyQueue;
            this.latch = latch;
        }
        @Override
        public void run() {
            try {
                fx = fy = fz = 0;
                for (Body otherBody : bodyQueue) {
                    if (!exists) {
                        // this body was collapsed into another by another thread
                        return;
                    }
                    if (Body.this != otherBody && otherBody.exists) {
                        calcForceFrom(otherBody);
                    }
                }
            } catch (Exception e) {
                logger.error("ForceComputer threw", e);
            } finally {
                latch.countDown();
            }
        }
    }

    /**
     * Subsume the other body into this body. Absorbs the other body's mass and adds its radius
     * to this instance's radius, and sets the other body's {@code exists} flag to false
     *
     * @param otherBody the other body to subsume into this body
     */
    private void subsume(Body otherBody) {
        synchronized (this) {
            mass += otherBody.mass;
            radius += otherBody.radius;
            synchronized (otherBody) {
                otherBody.mass = 0D;
                otherBody.exists = false;
            }
        }
        logger.info("Body ID {} subsumed ID {}", id, otherBody.id);
    }

    private void calcForceFrom(Body otherBody) {
        if (!exists || !otherBody.exists) {
            return;
        }
        // position values are updated via a single thread in the update method, so no concurrency guards here
        double dx = otherBody.x - x;
        double dy = otherBody.y - y;
        double dz = otherBody.z - z;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

        if (dist > 0.61D) {
            double force = (G * mass * otherBody.mass) / (dist * dist);
            // only one thread at a time will ever modify force values. If either this or other body
            // were subsumed and mass set to zero then the result will be a NOP here
            fx += force * dx / dist;
            fy += force * dy / dist;
            fz += force * dz / dist;
        } else {
            if (mass > otherBody.mass) {
                subsume(otherBody);
            } else {
                otherBody.subsume(this);
            }
        }
    }
}
