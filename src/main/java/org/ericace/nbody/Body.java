package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    /**
     * The gravitational constant
     */
    private static final double G = 6.673e-11;

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
     * If this is a sun, then it has a light source
     */
    private boolean isSun = false;

    /**
     * If this body is collapsed into another then this flag is set to false
     * and it will be removed from the simulation
     */
    private volatile boolean exists;

    /**
     * Supports modifying the instance from multiple threads
     */
    private final Lock lock;

    /**
     * Monotonically increasing ID generator
     *
     * @return next ID value starting at zero and incrementing on each call
     */
    static int nextID() {
        return IdGenerator.nextID();
    }

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
     * Sets the body to not exist. Eventually it will be removed from the simulation and from the
     * rendering engine's scene graph
     */
    void setNotExists() {
        mass = 0;
        exists = false;
    }

    /**
     * Sets this instance to a sun - the render engine should create an associated light source
     */
    void setSun() {
        isSun = true;
    }

    /**
     * @return true if this body is a sun, else false
     */
    boolean isSun() {
        return isSun;
    }

    /**
     * Creates an instance with passed configuration
     *
     * @param id     Every body should be created with a unique ID starting at zero with max < total
     *               bodies because this ID is also used as an id by the rendering engine. (The class
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
        lock = new ReentrantLock();
    }

    /**
     * Acquires a lock on this instance if no other thread has already acquired a lock
     *
     * @return true if the lock was acquired
     */
    boolean tryLock() {
        return lock.tryLock();
    }

    /**
     * Releases the lock
     */
    void unlock() {
        lock.unlock();
    }

    /**
     * Re-computes velocity and position from force accumulated as of the method call.
     *
     * Intended to be called such that the calling thread has exclusive access to the body. Therefore,
     * no concurrency control.
     *
     * @param timeScaling a smoothing factor. Larger numbers speed up the sim, and smaller number slow it down
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
     * Returns a {@link BodyRenderInfo} instance with values populated with info needed to render the body
     * by the graphics engine
     *
     * @return see {@link BodyRenderInfo}
     */
    BodyRenderInfo getRenderInfo() {
        return new BodyRenderInfo(id, x, y, z, radius, isSun);
    }

    /**
     * A Callable that a ThreadPoolExecutor can run to calculate the force on this body from all other
     * bodies.
     *
     * The design is that one thread updates force for an instance - therefore the thread can safely
     * write to the force instance fields without synchronization code. The only other place the
     * force variables are referenced is in the {@link #update} method which - again - by design,
     * runs in a single thread and is run at a different time by the {@code ComputationRunner} class and
     * therefore has exclusive access to the instance.
     *
     * The exception is when one body subsumes another body. There is thread synchronization there
     */
    class ForceComputer implements Callable<Void> {
        /**
         * A concurrent queue of bodies in the sim - including this one. Expectation is that this
         * queue is changing while the force computation occurs - i.e. objects are being added
         * and removed.
         */
        private final ConcurrentLinkedQueue<Body> bodyQueue;

        /**
         * Saves a ref to the passed body queue for the {@link #call} method
         *
         * @param bodyQueue the queue of bodies in the simulation
         */
        ForceComputer(ConcurrentLinkedQueue<Body> bodyQueue) {
            this.bodyQueue = bodyQueue;
        }

        /**
         * Calculates the force on this instance from all other instances in the simulation
         *
         * @return always null
         */
        @Override
        public Void call() {
            try {
                fx = fy = fz = 0;
                for (Body otherBody : bodyQueue) {
                    if (!exists) {
                        break;
                    }
                    if (Body.this != otherBody && otherBody.exists) {
                        calcForceFrom(otherBody);
                    }
                }
            } catch (Exception e) {
                logger.error("ForceComputer threw", e);
            }
            return null;
        }
    }

    /**
     * Subsumes another body into this body.
     *
     * Absorbs the other body's mass and adds its radius to this instance's radius, and sets
     * the other body's {@code exists} flag to false. This is the one method of the simulation with the
     * most thread contention. However, it happens relatively infrequently.
     *
     * Attempts to acquire two locks - first on this body and then on the other body. Only if both locks
     * are acquired does the operation succeed. Otherwise it is a NOP. The thinking here is - if an
     * attempt to acquire this object's lock fails then another thread is subsuming this instance. And
     * if the other instance's lock can't be acquired then that instance is being subsumed so do
     * nothing and let the first thread do the subsuming.
     *
     * The updates to mass and radius are not atomic. Worst case, another thread reading these values
     * won't see the updated values until next compute cycle. But since they are volatile, the write
     * is guaranteed to avoid a race condition. And - the only place that updates those values is here
     * and the write is guarded to there will never be contention on the writes.
     *
     * @param otherBody the other body to subsume into this body
     */
    private void subsume(Body otherBody) {
        boolean subsumed = false;
        if (tryLock()) {
            boolean otherLock = false;
            try {
                otherLock = otherBody.tryLock();
                if (otherLock) {
                    double volume = (4.1888D * radius*radius*radius) +
                            (4.1888D * otherBody.radius*otherBody.radius*otherBody.radius);
                    double newRadius = (Math.pow(0.62035D * volume, .333D) - radius) * .125D;
                    logger.info("old radius: {} -- new radius: {}", radius, newRadius);
                    radius = newRadius;
                    mass += otherBody.mass;
                    otherBody.setNotExists();
                    subsumed = true;
                }
            } finally {
                unlock();
                if (otherLock) {
                    otherBody.unlock();
                }
            }
        }
        if (subsumed) {
            logger.info("Body ID {} subsumed ID {}", id, otherBody.id);
        }
    }

    /**
     * Calculates force on this body from another body. If the bodies reach a hard-coded proximity, the larger
     * body subsumes the smaller body.
     *
     * @param otherBody the other body to calculate force from
     */
    private void calcForceFrom(Body otherBody) {
        double dx = otherBody.x - x;
        double dy = otherBody.y - y;
        double dz = otherBody.z - z;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
//        if (dist > 0.51D) {
        if (dist > (radius + otherBody.radius) * .125D) {
            double force = (G * mass * otherBody.mass) / (dist * dist);
            // only one thread at a time will ever modify force values. If either this or other body
            // were subsumed and mass set to zero then the result will be a NOP here
            fx += force * dx / dist;
            fy += force * dy / dist;
            fz += force * dz / dist;
        } else {
            logger.info("distance: {} -- this radius {}: -- other radius: {}", dist, radius, otherBody.radius);
            if (mass > otherBody.mass) {
                subsume(otherBody);
            } else {
                otherBody.subsume(this);
            }
        }
    }

    /**
     * Monotonically increasing thread-safe ID generator to generate unique IDs for each body
     */
    private static class IdGenerator {
        private volatile static int id = 0;
        static synchronized int nextID() {
            return id++;
        }
    }
}
