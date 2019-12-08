package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Models a body with position, velocity, radius, and mass. Calculates force exerted
 * on itself from other {@code Body} instances.
 * <p>
 * The three primary components of functionality are:</p>
 * <p>
 * The {@link ForceComputer} nested class, which is run by a {@code ThreadPoolExecutor} and which
 * calculates force from all other Body instances in the simulation.</p>
 * <p>
 * The {@link #update} method, which applies the calculated force to the body and computes a new position</p>
 * <p>
 * The {@link #subsume} method - which absorbs another body into an instance when they reach a certain
 * proximity. Since the main purpose of this Java app is to see how many bodies can be simulated based
 * on CPU/GPU configuration, I elected to combine bodies together if there is a collision, rather than
 * breaking them apart or just doing a collision and redirection. (That might change in a future
 * version.)</p>
 * <p>
 * This class borrows from: http://physics.princeton.edu/~fpretori/Nbody/code.htm</p>
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
     * @return the body ID
     */
    int getId() {
        return id;
    }

    /**
     * Getter for {@link #exists}
     */
    boolean exists() {
        return exists;
    }

    /**
     * Sets the body to not exist
     */
    void setNotExists() {
        synchronized (this) {
            mass = 0;
            exists = false;
        }
    }

    /**
     * Creates an instance with passed configuration
     *
     * @param id     Every body should be created with a unique ID starting at zero with max < total bodies
     *               because this ID is also used as an array index by the rendering engine. (The class
     *               does not enforce this)
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
     * Re-computes velocity and position from force accumulated as of the method call.
     *
     * Intended to be called from a single thread - so no two bodies are ever computed concurrently
     * in different threads. Therefore, no concurrency control.
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
     * A Runnable that a ThreadPoolExecutor can run to calculate the force on this body from all other
     * bodies.
     *
     * The design is that one thread updates force for an instance - therefore the thread can safely
     * write to the force class variables without synchronization code. The only other place the
     * force variables are referenced is in the {@link #update} method which - again - by design,
     * runs in a single thread and is run at a different time by the {@code ComputationRunner} class.
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
                if (!exists) {
                    // this body was collapsed into another by another thread
                    return;
                }
                fx = fy = fz = 0;
                for (Body otherBody : bodyQueue) {
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
     * Subsumes another body into this body.
     *
     * Absorbs the other body's mass and adds its radius to this instance's radius, and sets
     * the other body's {@code exists} flag to false. This is the one method of the simulation with the
     * most thread contention. However, it happens relatively infrequently.
     *
     * @param otherBody the other body to subsume into this body
     */
    private void subsume(Body otherBody) {
        synchronized (this) {
            mass += otherBody.mass;
            radius += otherBody.radius;
            otherBody.setNotExists();
        }
        logger.info("Body ID {} subsumed ID {}", id, otherBody.id);
    }

    /**
     * Calculates force on this body from another body. If the bodies reach a hard-coded proximity, the larger
     * body subsumes the smaller body.
     *
     * TODO: implement collision instead.
     *
     * @param otherBody the other body to calculate force from
     */
    private void calcForceFrom(Body otherBody) {
        if (!exists || !otherBody.exists) {
            return;
        }
        // position values are updated via a single thread in the update method, so no concurrency guards here
        double dx = otherBody.x - x;
        double dy = otherBody.y - y;
        double dz = otherBody.z - z;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

        if (dist > 0.51D) {
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
