package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

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
 * The {@link Body.ForceComputer} nested class, which is run by a {@code ThreadPoolExecutor} and which
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
public class Body {
    private static final Logger logger = LogManager.getLogger(Body.class);

    private static final Level CUSTOM = Level.getLevel("CUSTOM");
    /**
     * The gravitational constant
     */
    private static final double G = 6.673e-11;

    // some PI-related constants
    private static final double FOUR_THIRDS_PI = Math.PI * (4.0D/3.0D);
    private static final double FOUR_PI = Math.PI * 4;

    /**
     * Coefficient of restitution
     */
    private volatile static double R = 1;

    /**
     * When a body is fragmented, this is set to false for one compute cycle so the simulation
     * doesn't get caught trying to successively fragment bodies
     */
    private boolean allowCollision = true;

    /**
     * A unique ID value for each instance
     */
    private final int id;

    /**
     * Set to True whenever this object collided with another
     */
    private volatile boolean collided;

    /**
     * Radius and mass
     */
    private volatile double radius, mass;

    /**
     * TODO make configurable
     */
    private final double fragmentationStep = 200;

    /**
     * Fragmentation factor
     */
    private double fragFactor = 1;

    /**
     * current coordinates of the body
     */
    private volatile double x, y, z;

    /**
     * cumulative velocity
     */
    private volatile double vx, vy, vz;

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
     * Defines the supported collision responses. SUBSUME means that two bodies merge into one
     * upon collision: the larger mass body subsumes the smaller. ELASTIC_COLLISION means that the
     * bodies bounce off each other. FRAGMENT means a body breaks into smaller bodies upon collision
     * NONE means no collisions - bodies pass through each other
     */
    public enum CollisionBehavior {
        NONE, SUBSUME, ELASTIC, FRAGMENT
    }

    /**
     * The collision behavior
     */
    private final CollisionBehavior collisionBehavior;

    /**
     * Defines supported colors
     */
    public enum Color {
        RANDOM, BLACK, WHITE, DARKGRAY, GRAY, LIGHTGRAY, RED, GREEN, BLUE, YELLOW, MAGENTA, CYAN, ORANGE, BROWN, PINK
    }

    /**
     * Body color
     */
    private final Color color;

    /**
     * Monotonically increasing ID generator
     *
     * @return next ID value starting at zero and incrementing on each call
     */
    public static int nextID() {
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
    public boolean exists() {
        return exists;
    }

    /**
     * Sets the body to not exist. Eventually it will be removed from the simulation and from the
     * rendering engine's scene graph
     */
    public void setNotExists() {
        mass = 0;
        exists = false;
    }

    /**
     * Sets this instance to a sun - the render engine should create an associated light source
     */
    public void setSun() {
        isSun = true;
    }

    /**
     * @return true if this body is a sun, else false
     */
    public boolean isSun() {
        return isSun;
    }

    /**
     * Sets the coefficient of restitution
     *
     * @param R the new coefficient
     */

    public static void setRestitutionCoefficient(double R) {
        Body.R = R;
    }

    /**
     * @return the  coefficient of restitution
     */
    public static double getRestitutionCoefficient() {
        return Body.R;
    }

    /**
     * Creates an instance configured for elastic collision
     *
     * @see Body#Body(int, double, double, double, double, double, double, double, float, CollisionBehavior, Color, double)
     */
    public Body(int id, double x, double y, double z, double vx, double vy, double vz, double mass, float radius) {
        this(id, x, y, z, vx, vy, vz, mass, radius, CollisionBehavior.ELASTIC, Color.RANDOM, 1);
    }

    /**
     * Creates an instance with passed configuration
     *
     * @param id                Every body should be created with a unique ID starting at zero with max < total
     *                          bodies because this ID is also used as an id by the rendering engine. (The class
     *                          does not enforce this)
     * @param x                 Position
     * @param y                 "
     * @param z                 "
     * @param vx                Velocity
     * @param vy                "
     * @param vz                "
     * @param mass              Mass
     * @param radius            Radius
     * @param collisionBehavior The collision behavior for the body
     * @param color             Body color
     * @param fragFactor        Fragmentation factor
     */
    public Body(int id, double x, double y, double z, double vx, double vy, double vz, double mass, float radius,
                CollisionBehavior collisionBehavior, Color color, double fragFactor) {
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
        this.collisionBehavior = collisionBehavior;
        this.color             = color;
        this.fragFactor        = fragFactor;
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
     * @param timeScaling a smoothing factor. Larger numbers speed up the sim; smaller numbers slow it down
     *
     * @return see {@link BodyRenderInfo}
     */
    BodyRenderInfo update(double timeScaling) {
        if (!exists) {
            // creates an instance with exists=false so the graphics engine will remove it from the scene
            return new BodyRenderInfo(id);
        }
        if (!collided) { // TEST TEST TEST
            vx += timeScaling * fx / mass;
            vy += timeScaling * fy / mass;
            vz += timeScaling * fz / mass;
        }
        x += timeScaling * vx;
        y += timeScaling * vy;
        z += timeScaling * vz;
        // clear collided flag for next cycle
        collided = false;
        allowCollision = true;
        return getRenderInfo();
    }

    /**
     * Returns a {@link BodyRenderInfo} instance with values populated with info needed to render the body
     * by the graphics engine
     *
     * @return see {@link BodyRenderInfo}
     */
    BodyRenderInfo getRenderInfo() {
        return new BodyRenderInfo(id, x, y, z, radius, isSun, color);
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
         * Calculates the force on this instance from all other instances in the simulation. The body could
         * be set to not exist, or, could collide with another body at any time. If the body becomes non-existent
         * then stop the calculation.
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
//                    if (Body.this != otherBody && otherBody.exists && !skipComputation && !otherBody.skipComputation) {
                    if (Body.this != otherBody && otherBody.exists) {
                        ForceCalcResult result = calcForceFrom(otherBody);
                        if (result.collided) {
                            resolveCollision(result.dist, otherBody, bodyQueue);
                        }
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
     * Absorbs the other body's mass and adjusts this instance's radius accordingly, and sets
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
     * @param dist      The distance between the bodies
     * @param otherBody the other body to subsume into this body
     */
    private void subsume(double dist, Body otherBody) {
        if (dist + otherBody.radius >= radius * 1.2) {
            // only if  most of the other body is inside this body
            return;
        }
        boolean subsumed = false;
        double thisMass=0, otherMass=0;
        if (tryLock()) {
            boolean otherLock = false;
            try {
                otherLock = otherBody.tryLock();
                if (otherLock) {
                    thisMass = mass;
                    otherMass = otherBody.mass;
                    double volume = (FOUR_THIRDS_PI * radius * radius * radius) +
                            (FOUR_THIRDS_PI * otherBody.radius * otherBody.radius * otherBody.radius);
                    double newRadius = Math.pow((volume * 3.0D) / FOUR_PI, 1.0D / 3.0D);
                    logger.info("old radius: {} -- new radius: {}", radius, newRadius);
                    // TODO TEST put this back
                    //radius = newRadius;
                    //radius *= 1.2D;
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
            logger.info("Body ID {} (mass {}) subsumed ID {} (mass {})", id, thisMass, otherBody.id, otherMass);
        }
    }

    /**
     * Calculates force on this body from another body. If the bodies collide, resolves the collision
     *
     * @param otherBody the other body to calculate force from
     */
    private ForceCalcResult calcForceFrom(Body otherBody) {
        double dx = otherBody.x - x;
        double dy = otherBody.y - y;
        double dz = otherBody.z - z;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        // Only allow one collision per body per cycle. Once a collision happens, continue to apply gravitational
        // force to the collided body. Allowing a body to collide multiple times caused odd things to happen
        // when many bodies were tightly compacted (not sure why) and it also impacts performance - lots of additional
        // calculations
        if (collided || dist > (radius + otherBody.radius)) {
            double force = (G * mass * otherBody.mass) / (dist * dist);
            // only one thread at a time will ever modify force values. If either this or other body
            // were subsumed and mass set to zero then the result will be a NOP here
            fx += force * dx / dist;
            fy += force * dy / dist;
            fz += force * dz / dist;
        } else if (allowCollision && otherBody.allowCollision && dist <= (radius + otherBody.radius)) {
            logger.info("collision: distance: {} -- this radius {}: -- other radius: {} -- this id: {} -- other id: {}",
                    dist, radius, otherBody.radius, id, otherBody.id);
            return ForceCalcResult.collision(dist);
        }
        return ForceCalcResult.noCollision();
    }

    /**
     * Resolves collisions between two bodies according to the {@link #collisionBehavior}
     * field. If collision type is NONE, then nothing happens and the bodies pass through each other. While
     * impossible in the real world, it provides some interesting effects.
     *
     * @param dist      distance between bodies
     * @param otherBody the other body being collided with
     * @param bodyQueue The body queue - in case the collision causes fragmentation resulting in  bodies being
     *                  added to the queue
     */
    private void resolveCollision(double dist, Body otherBody, ConcurrentLinkedQueue<Body> bodyQueue) {
        if (collisionBehavior == CollisionBehavior.SUBSUME ||
                otherBody.collisionBehavior == CollisionBehavior.SUBSUME) {
            // arbitrarily, larger bodies always subsume smaller bodies
            if (radius > otherBody.radius) {
                subsume(dist, otherBody);
            } else {
                otherBody.subsume(dist, this);
            }
        } else if ((collisionBehavior == CollisionBehavior.ELASTIC || collisionBehavior == CollisionBehavior.FRAGMENT)
                && (otherBody.collisionBehavior == CollisionBehavior.ELASTIC ||
                otherBody.collisionBehavior == CollisionBehavior.FRAGMENT)) {
            CollisionCalcResult r = calcElasticCollision(otherBody);
            if (r.collided) {
                if (tryLock()) {
                    boolean otherLock = false;
                    try {
                        otherLock = otherBody.tryLock();
                        if (otherLock && exists && otherBody.exists) {
                            FragmentationCalcResult fr = shouldFragment(otherBody, r);
                            if (fr.shouldFragment) {
                                doFragment(otherBody, fr, bodyQueue);
                            } else {
                                doElastic(otherBody, r);
                            }
                        }
                    } finally {
                        unlock();
                        if (otherLock) {
                            otherBody.unlock();
                        }
                    }
                }
                if (collided) {
                    logger.info("Body ID {} collided with ID {}", id, otherBody.id);
                }
            }
        }
    }

    /**
     * Updates velocity in each instance and sets the collided flag
     *
     * @param otherBody the other body this body is colliding with
     * @param r         the result of the elastic collision calc that establishes new velocities for
     *                  colliding bodies
     */
    private void doElastic(Body otherBody, CollisionCalcResult r) {
        vx = (r.vx1 - r.vx_cm) * R + r.vx_cm;
        vy = (r.vy1 - r.vy_cm) * R + r.vy_cm;
        vz = (r.vz1 - r.vz_cm) * R + r.vz_cm;
        otherBody.vx = (r.vx2 - r.vx_cm) * R + r.vx_cm;
        otherBody.vy = (r.vy2 - r.vy_cm) * R + r.vy_cm;
        otherBody.vz = (r.vz2 - r.vz_cm) * R + r.vz_cm;
        collided = otherBody.collided = true;
    }

    /**
     * Resolves an elastic collision with another body. This code was adapted with only minor mods - mostly
     * formatting - from the following resource:
     *
     * https://www.plasmaphysics.org.uk/programs/coll3d_cpp.htm
     *
     * @param otherBody the other body being collided with
     *
     * @return the result of the calculation
     */
    /*private*/ CollisionCalcResult calcElasticCollision(Body otherBody) {
        double r12, m21, d, v, theta2, phi2, st, ct, sp, cp, vx1r, vy1r, vz1r, fvz1r,
                thetav, phiv, dr, alpha, beta, sbeta, cbeta, t, a, dvz2,
                vx2r, vy2r, vz2r, x21, y21, z21, vx21, vy21, vz21, vx_cm, vy_cm, vz_cm;

        double m1 = mass;
        double m2 = otherBody.mass;
        double r1 = radius;
        double r2 = otherBody.radius;
        double x1 = x;
        double y1 = y;
        double z1 = z;
        double x2 = otherBody.x;
        double y2 = otherBody.y;
        double z2 = otherBody.z;
        double vx1 = vx;
        double vy1 = vy;
        double vz1 = vz;
        double vx2 = otherBody.vx;
        double vy2 = otherBody.vy;
        double vz2 = otherBody.vz;

        r12 = r1 + r2;
        m21 = m2 / m1;
        x21 = x2 - x1;
        y21 = y2 - y1;
        z21 = z2 - z1;
        vx21 = vx2 - vx1;
        vy21 = vy2 - vy1;
        vz21 = vz2 - vz1;

        vx_cm = (m1 * vx1 + m2 * vx2) / (m1 + m2);
        vy_cm = (m1 * vy1 + m2 * vy2) / (m1 + m2);
        vz_cm = (m1 * vz1 + m2 * vz2) / (m1 + m2);

        // calculate relative distance and relative speed
        d = Math.sqrt(x21*x21 + y21*y21 + z21*z21);
        v = Math.sqrt(vx21*vx21 + vy21*vy21 + vz21*vz21);

        // comment out - if the radii overlap run the calc anyway
        // return if distance between balls smaller than sum of radii
        //if (d < r12) {return;}

        // return if relative speed = 0
        if (v == 0) {
            logger.info("Exit elastic collision: v == 0. This id: {} -- other id: {}", id, otherBody.id);
            return CollisionCalcResult.noCollision();
        }

        // shift coordinate system so that ball 1 is at the origin
        x2 = x21;
        y2 = y21;
        z2 = z21;

        // boost coordinate system so that ball 2 is resting
        vx1 = -vx21;
        vy1 = -vy21;
        vz1 = -vz21;

        // find the polar coordinates of the location of ball 2
        theta2 = Math.acos(z2/d);
        if (x2 == 0 && y2 == 0) {
            phi2 = 0;
        } else {
            phi2=Math.atan2(y2, x2);
        }
        st = Math.sin(theta2);
        ct = Math.cos(theta2);
        sp = Math.sin(phi2);
        cp = Math.cos(phi2);

        // express the velocity vector of ball 1 in a rotated coordinate system where ball 2 lies on the z-axis
        vx1r = ct * cp * vx1 + ct * sp * vy1 - st * vz1;
        vy1r = cp * vy1 - sp * vx1;
        vz1r = st * cp * vx1 + st * sp * vy1 + ct * vz1;
        fvz1r = vz1r / v;
        if (fvz1r > 1) {
            // fix for possible rounding errors
            fvz1r=1;
        } else if (fvz1r < -1) {
            fvz1r=-1;
        }
        thetav=Math.acos(fvz1r);
        if (vx1r == 0 && vy1r == 0) {
            phiv=0;
        } else {
            phiv = Math.atan2(vy1r,vx1r);
        }

        // calculate the normalized impact parameter
        dr = d * Math.sin(thetav) / r12;

        // if balls do not collide, do nothing
        if (thetav > Math.PI / 2 || Math.abs(dr) > 1) {
            logger.info("Bodies do not collide. This id: {} -- other id: {}", id, otherBody.id);
            return CollisionCalcResult.noCollision();
        }

        // calculate impact angles if balls do collide
        alpha = Math.asin(-dr);
        beta = phiv;
        sbeta = Math.sin(beta);
        cbeta = Math.cos(beta);

        // comment out - position is assigned in the update method
        // calculate time to collision
        //t = (d * Math.cos(thetav) - r12 * Math.sqrt(1 - dr * dr)) / v;
        // update positions and reverse the coordinate shift
        // x2 = x2 + vx2 * t + x1;
        // y2 = y2 + vy2 * t + y1;
        // z2 = z2 + vz2 * t + z1;
        // x1 = (vx1 + vx2) * t + x1;
        // y1 = (vy1 + vy2) * t + y1;
        // z1 = (vz1 + vz2) * t + z1;

        // update velocities

        a = Math.tan(thetav + alpha);

        dvz2 = 2 * (vz1r + a * (cbeta * vx1r + sbeta * vy1r)) / ((1 + a * a) * (1 + m21));

        vz2r = dvz2;
        vx2r = a * cbeta * dvz2;
        vy2r = a * sbeta * dvz2;
        vz1r = vz1r - m21 * vz2r;
        vx1r = vx1r - m21 * vx2r;
        vy1r = vy1r - m21 * vy2r;

        // rotate the velocity vectors back and add the initial velocity
        // vector of ball 2 to retrieve the original coordinate system

        return CollisionCalcResult.collision(
          ct * cp * vx1r - sp * vy1r + st * cp * vz1r + vx2,
          ct * sp * vx1r + cp * vy1r + st * sp * vz1r + vy2,
          ct * vz1r - st * vx1r                       + vz2,
          ct * cp * vx2r - sp * vy2r + st * cp * vz2r + vx2,
          ct * sp * vx2r + cp * vy2r + st * sp * vz2r + vy2,
          ct * vz2r - st * vx2r                       + vz2,
          vx_cm, vy_cm, vz_cm);
    }

    /**
     * If either body involved in a collision is configured for fragmentation collision, then determine if
     * the force of the collision passes a threshold configured in the body. This enables bodies to be more - or
     * less - likely to fragment. It's kind of a "hardness" factor. The logic looks at change in velocity: Two
     * bodies with equal mass and velocity approaching from exact opposite directions will - in a pure elastic
     * collision - exchange velocities resulting in a fragmentation factor of 1. If the body's fragmentation
     * factor is also 1, then the collision will not cause fragmentation.
     *
     * Note that a very high speed body impacting a very low speed body will drastically change the slower body's
     * speed resulting in a very high frag factor. E.g. a body could be traveling at -100 X and be collided with
     * and move at 1e20 X.
     *
     * @param otherBody the other body being collided with
     * @param r         the result of the collision calculation
     *
     * @return the result of the calculation
     */
    /*private*/ FragmentationCalcResult shouldFragment(Body otherBody, CollisionCalcResult r) {
        if (!(collisionBehavior == CollisionBehavior.FRAGMENT ||
                otherBody.collisionBehavior == CollisionBehavior.FRAGMENT)) {
            return FragmentationCalcResult.noFragmentation();
        }
        double vThis = vx + vy + vz;
        double dvThis =
                Math.abs(vx - ((r.vx1 - r.vx_cm) * R + r.vx_cm)) +
                Math.abs(vy - ((r.vy1 - r.vy_cm) * R + r.vy_cm)) +
                Math.abs(vz - ((r.vz1 - r.vz_cm) * R + r.vz_cm));
        double vThisFactor = dvThis / Math.abs(vThis);
        double vOther = otherBody.vx + otherBody.vy + otherBody.vz;
        double dvOther =
                Math.abs(otherBody.vx - ((r.vx2 - r.vx_cm) * R + r.vx_cm)) +
                Math.abs(otherBody.vy - ((r.vy2 - r.vy_cm) * R + r.vy_cm)) +
                Math.abs(otherBody.vz - ((r.vz2 - r.vz_cm) * R + r.vz_cm));
        double vOtherFactor = dvOther / Math.abs(vOther);
        //logger.log(CUSTOM, "this.id={} vThis=({},{},{})", id, vx, vy, vz);
        if ((collisionBehavior == CollisionBehavior.FRAGMENT && vThisFactor > fragFactor) ||
                (otherBody.collisionBehavior == CollisionBehavior.FRAGMENT && vOtherFactor > otherBody.fragFactor)) {
            return FragmentationCalcResult.fragmentation(vThisFactor, vOtherFactor);
        }
        return FragmentationCalcResult.noFragmentation();
    }

    /**
     * Handles a collision where one of the bodies is configured for fragmentation
     *
     * @param otherBody the other body being collided with
     * @param fr        results of fragmentation calc
     * @param bodyQueue the body queue in case new bodies need to be added
     */
    private void doFragment(Body otherBody, FragmentationCalcResult fr, ConcurrentLinkedQueue<Body> bodyQueue) {
        if (collisionBehavior == CollisionBehavior.FRAGMENT && fr.thisFactor > fragFactor) {
            implementFragmentation(fr.thisFactor, bodyQueue);
        }
        if (otherBody.collisionBehavior == CollisionBehavior.FRAGMENT && fr.otherFactor > otherBody.fragFactor) {
            otherBody.implementFragmentation(fr.otherFactor, bodyQueue);
        }
    }

    /**
     * TODO - WIP - Occasionally creates too many bodies too quickly and swamps the sim
     *
     * Sets this body to not exist, and creates a number of smaller bodies occupying the same space, thus
     * appearing to fragment the instance into smaller bodies
     *
     * @param fragFactor The calculated fragmentation factor based on velocity change for this body
     * @param bodyQueue  the body queue to add new bodies to
     */
    private void implementFragmentation(double fragFactor, ConcurrentLinkedQueue<Body> bodyQueue) {
        exists = false;
        double volume = (FOUR_THIRDS_PI * radius * radius * radius);
        double fragDelta = fragFactor - this.fragFactor;
        if (fragDelta > 10) fragDelta = 10;
        int fragments = (int) (fragDelta * fragmentationStep);
        if (fragments > 200) {
            // just a hard-coded limit to avoid swamping the sim with too many bodies
            fragments = 200;
        }
        volume /= fragments;
        double newRadius = Math.pow((volume * 3.0D) / FOUR_PI, 1.0D / 3.0D);
        for (int i = 0; i < fragments; ++i) {
            SimpleVector v = SimpleVector.getVectorEven(new SimpleVector((float) x, (float) y, (float) z), radius * .9);
            // as we add bodies, we increase the frag factor so bodies become harder to sub-divide and also,
            // we don't subdivide smaller than a certain size (don't create a sim filled with dust)
            CollisionBehavior ncb = newRadius <= .25 ? CollisionBehavior.ELASTIC : CollisionBehavior.FRAGMENT;
            Body b = new Body(Body.nextID(), v.x, v.y, v.z, vx, vy, vz, mass / fragments, (float) newRadius, ncb,
                    color, fragFactor * 30);
            b.allowCollision = false;
            bodyQueue.add(b);
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

    /**
     * Helper that holds values associated with calculating fragmentation during body collision
     */
    /*private*/ static class FragmentationCalcResult {
        final boolean shouldFragment;
        final double thisFactor;
        final double otherFactor;
        private FragmentationCalcResult(boolean shouldFragment, double thisFactor, double otherFactor) {
            this.shouldFragment = shouldFragment;
            this.thisFactor     = thisFactor;
            this.otherFactor    = otherFactor;
        }
        static FragmentationCalcResult noFragmentation() {
            return new FragmentationCalcResult(false, 0, 0);
        }
        static FragmentationCalcResult fragmentation(double thisFactor, double otherFactor) {
            return new FragmentationCalcResult(true, thisFactor, otherFactor);
        }
    }

    /**
     * Helper that holds values associated with calculating gravitational force between two bodies.
     */
    private static class ForceCalcResult {
        /**
         * Distance between bodies
         */
        final double dist;

        /**
         * True if force calculation determined that the bodies are colliding (radii overlap), else False
         */
        final boolean collided;

        private ForceCalcResult(double dist, boolean collided) {
            this.dist = dist;
            this.collided = collided;
        }
        static ForceCalcResult noCollision() {
            return new ForceCalcResult(0, false);
        }
        static ForceCalcResult collision(double dist) {
            return new ForceCalcResult(dist, true);
        }
    }

    /**
     * Helper that holds values associated with the elastic collision calculation
     */
    /*private*/ static class CollisionCalcResult {
        final boolean collided;
        final double vx1;
        final double vy1;
        final double vz1;
        final double vx2;
        final double vy2;
        final double vz2;
        final double vx_cm;
        final double vy_cm;
        final double vz_cm;
        private CollisionCalcResult() {
            collided = false;
            vx1 = vy1 = vz1 = vx2 = vy2 = vz2 = vx_cm = vy_cm = vz_cm = 0;
        }
        private CollisionCalcResult(double vx1, double vy1, double vz1, double vx2, double vy2, double vz2,
                                    double vx_cm, double vy_cm, double vz_cm) {
            collided = true;
            this.vx1   = vx1;
            this.vy1   = vy1;
            this.vz1   = vz1;
            this.vx2   = vx2;
            this.vy2   = vy2;
            this.vz2   = vz2;
            this.vx_cm = vx_cm;
            this.vy_cm = vy_cm;
            this.vz_cm = vz_cm;
        }
        static CollisionCalcResult noCollision() {
            return new CollisionCalcResult();
        }
        static CollisionCalcResult collision(double vx1, double vy1, double vz1, double vx2, double vy2, double vz2,
                                      double vx_cm, double vy_cm, double vz_cm) {
            return new CollisionCalcResult(vx1, vy1, vz1, vx2, vy2, vz2, vx_cm, vy_cm, vz_cm);
        }
    }
}
