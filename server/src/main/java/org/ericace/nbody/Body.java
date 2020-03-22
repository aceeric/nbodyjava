package org.ericace.nbody;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ericace.globals.Globals;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Models a body with position, velocity, radius, and mass. Calculates force exerted on itself from other
 * {@code Body} instances. Supports collision resolution between bodies.
 * <p>
 * This class borrows from: http://physics.princeton.edu/~fpretori/Nbody/code.htm</p>
 */
public class Body {
    private static final Logger logger = LogManager.getLogger(Body.class);

    private static final Level CUSTOM = Level.getLevel("CUSTOM");

    /**
     * The gravitational constant
     */
    private static final double G = 6.673e-11F;

    // some PI-related constants
    private static final float FOUR_THIRDS_PI = (float) Math.PI * (4F/3F);
    private static final float FOUR_PI = (float) Math.PI * 4F;

    /**
     * Coefficient of restitution
     */
    private volatile static float R = 1;

    /**
     * A unique ID value for each instance
     */
    private final int id;

    /**
     * A user-friendly name
     */
    private final String name;

    /**
     * A class
     */
    private final String clas;

    /**
     * Set to True whenever this object collided with another
     */
    private volatile boolean collided;

    /**
     * Radius and mass
     */
    private volatile float radius, mass;

    /**
     * When the body fragments, how many sub-bodies are created per frag factor
     */
    private float fragmentationStep;

    /**
     * The max number of bodies that a body can fragment into each cycle without impacting the frame rate
     */
    private static final int MAX_FRAGS_PER_CYCLE = 100;

    /**
     * When a body fragments, set an arbitrary limit on the number of bodies it can fragment into so the sim
     * isn't swamped
     */
    private static final int MAX_FRAGS = 2000;

    /**
     * True while a body is fragmenting because of a collision: In order to avoid overloading a single compute cycle,
     * a body can fragment in stages across consecutive cycles. The only other option I could come up
     * with was to fragment all at once which caused the sim to suspend noticeably when a body fragmented into
     * a large number of bodies. This is a compromise
     */
    private boolean fragmenting;

    /**
     * Holds info that support fragmenting a body across cycles so that the frame rate isn't slowed
     */
    private FragInfo fragInfo;

    /**
     * Fragmentation factor
     */
    private float fragFactor;

    /**
     * current coordinates of the body
     */
    private volatile float x, y, z;

    /**
     * cumulative velocity
     */
    private volatile float vx, vy, vz;

    /**
     * force on this body from all other bodies - zeroed and re-computed once per sim cycle
     */
    private double fx, fy, fz;

    /**
     * If this is a sun, then it has a light source
     */
    private boolean isSun = false;

    /**
     * If set to false the body will be removed from the simulation and the rendering scene graph
     */
    private volatile boolean exists;

    /**
     * Supports modifying the instance from multiple threads
     */
    private final Lock lock;

    /**
     * Supports test/debug
     */
    private boolean withTelemetry;

    /**
     * Don't delete from sim when the gRPC delete bodies method is invoked, unless the method
     * is invoked with -1 as the count to remove, in which case everything is removed no matter what
     */
    private boolean pinned;

    private Globals.CollisionBehavior collisionBehavior;
    /**
     * Body color
     */
    private Globals.Color color;

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
    public int getId() {
        return id;
    }

    /**
     * @return the body name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the body class
     */
    public String getClas() {
        return clas;
    }

    /**
     * @return whether the body is pinned
     */
    public boolean isPinned() {
        return pinned;
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

    public void setCollisionBehavior(Globals.CollisionBehavior behavior) {
        this.collisionBehavior = behavior;
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

    public static void setRestitutionCoefficient(float R) {
        Body.R = R;
    }

    /**
     * @return the  coefficient of restitution
     */
    public static float getRestitutionCoefficient() {
        return Body.R;
    }

    // more getters to support the gRPC API
    public float getX() {return x;}
    public float getY() {return y;}
    public float getZ() {return z;}
    public float getVx() {return vx;}
    public float getVy() {return vy;}
    public float getVz() {return vz;}
    public float getMass() {return mass;}
    public float getRadius() {return radius;}
    public Globals.CollisionBehavior getCollisionBehavior() {return collisionBehavior;}
    public Globals.Color getBodyColor() {return color;}
    public float getFragFactor() {return fragFactor;}
    public float getFragStep() {return fragmentationStep;}
    public boolean getWithTelemetry() {return withTelemetry;}

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
     * @param fragmentationStep The number of bodies to fragment into
     * @param withTelemetry     Allows for emitting telemetry about a particular body for testing/debugging
     * @param name              Give a body a unique name to enable modifying its properties using the client
     * @param clas              " class, like "asteroid" to enable modifying sets of bodies
     * @param pinned            If true, then the remove-bodies client command won't remove the body unless
     *                          remove-bodies -1 is specified which force removes everything
     */
    public Body(int id, float x, float y, float z, float vx, float vy, float vz, float mass, float radius,
                Globals.CollisionBehavior collisionBehavior, Globals.Color color, float fragFactor, float fragmentationStep,
                boolean withTelemetry, String name, String clas, boolean pinned) {
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
        this.fragmentationStep = fragmentationStep;
        lock = new ReentrantLock();
        this.withTelemetry = withTelemetry;
        this.name = name;
        this.clas = clas;
        this.pinned = pinned;
    }

    /**
     * Modify body properties while the simulation is running
     *
     * @param bodyMods the list of modifications to make
     *
     * @return true if the instance lock could be acquired and the mods made
     */
    public boolean mod(List<BodyMod> bodyMods) {
        boolean modified = false;
        if (tryLock()) {
            try {
                for (BodyMod bodyMod : bodyMods) {
                    switch (bodyMod.getMod()) {
                        case X: x = bodyMod.getFloat(); break;
                        case Y: y = bodyMod.getFloat(); break;
                        case Z: z = bodyMod.getFloat(); break;
                        case VX: vx = bodyMod.getFloat(); break;
                        case VY: vy = bodyMod.getFloat(); break;
                        case VZ: vz = bodyMod.getFloat(); break;
                        case MASS: mass = bodyMod.getFloat(); break;
                        case RADIUS: radius = bodyMod.getFloat(); break;
                        // TODO MAYBE REMOVE THIS OTHERWISE HAS TO BE SUPPORTED IN JME BUT CURRENTLY NOT FUNCTIONAL
                        case SUN: isSun = bodyMod.getBoolean(); break;
                        case COLLISION: collisionBehavior = bodyMod.getCollision(); break;
                        case COLOR: color = bodyMod.getColor(); break;
                        case FRAG_FACTOR: fragFactor = bodyMod.getFloat(); break;
                        case FRAG_STEP: fragmentationStep = bodyMod.getFloat(); break;
                        case TELEMETRY: withTelemetry = bodyMod.getBoolean(); break;
                        case EXISTS: exists = bodyMod.getBoolean(); break;
                    }
                }
                modified = true;
            } finally {
                unlock();
            }
        }
        return modified;
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
        if (!collided) {
            // the collision occurs in parallel with force computation so the force computation may not apply
            // to the velocity established by the collision calc. So - if this body collided, don't adjust the
            // velocity based on gravitational force. This is a fudge but - can't think of a better way to do it
            vx += timeScaling * fx / mass;
            vy += timeScaling * fy / mass;
            vz += timeScaling * fz / mass;
        }
        x += timeScaling * vx;
        y += timeScaling * vy;
        z += timeScaling * vz;
        // clear collided flag for next cycle
        collided = false;
        if (withTelemetry) {
            System.out.println(String.format("id:%d x:%f y:%f z:%f vx:%f vy:%f vz:%f m:%f r:%f", id, x, y, z,
                    vx, vy, vz, mass, radius));
        }
        if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z)) {
            logger.error("NaN values. ID={} (removing from sim)", id);
            exists = false;
        }
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
                if (fragmenting) {
                    fragment(bodyQueue);
                } else {
                    fx = fy = fz = 0;
                    for (Body otherBody : bodyQueue) {
                        if (!exists || fragmenting) {
                            break;
                        }
                        if (Body.this != otherBody && otherBody.exists && !otherBody.fragmenting) {
                            // calcForceFrom updates fx,fy,fz
                            ForceCalcResult result = calcForceFrom(otherBody);
                            if (result.collided) {
                                resolveCollision(result.dist, otherBody);
                            }
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
    private void subsume(float dist, Body otherBody) {
        if (dist + otherBody.radius >= radius * 1.2) {
            // only if  most of the other body is inside this body
            return;
        }
        boolean subsumed = false;
        float thisMass=0, otherMass=0;
        if (tryLock()) {
            boolean otherLock = false;
            try {
                otherLock = otherBody.tryLock();
                if (otherLock) {
                    thisMass = mass;
                    otherMass = otherBody.mass;
                    // TODO:
                    // If I allow the radius to grow it occasionally causes a runaway condition in which a body
                    // swallows the entire simulation. Need to figure this out
                    /*
                    float volume = (FOUR_THIRDS_PI * radius * radius * radius) +
                            (FOUR_THIRDS_PI * otherBody.radius * otherBody.radius * otherBody.radius);
                    float newRadius = (float) Math.pow((volume * 3F) / FOUR_PI, 1F/ 3F);
                    logger.info("old radius: {} -- new radius: {}", radius, newRadius);
                    //radius = newRadius;
                    radius *= 1.2D; // ?
                    */
                    mass = thisMass + otherMass;
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
        float dx = otherBody.x - x;
        float dy = otherBody.y - y;
        float dz = otherBody.z - z;
        float dist = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        // Only allow one collision per body per cycle. Once a collision happens, continue to apply gravitational
        // force to the collided body. Allowing a body to collide multiple times caused odd things to happen
        // when many bodies were tightly compacted (not sure why) and it also impacts performance - the collision
        // calculation is expensive. This is a compromise
        if (collided || dist > (radius + otherBody.radius)) {
            double force = (G * mass * otherBody.mass) / (dist * dist);
            // only one thread at a time will ever modify force values. If either this or other body
            // were subsumed and mass set to zero then the result will be a NOP here
            fx += force * dx / dist;
            fy += force * dy / dist;
            fz += force * dz / dist;
        } else if (dist <= (radius + otherBody.radius)) {
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
     */
    private void resolveCollision(float dist, Body otherBody) {
        if (collisionBehavior == Globals.CollisionBehavior.SUBSUME ||
                otherBody.collisionBehavior == Globals.CollisionBehavior.SUBSUME) {
            // arbitrarily, larger bodies always subsume smaller bodies
            if (radius > otherBody.radius) {
                subsume(dist, otherBody);
            } else {
                otherBody.subsume(dist, this);
            }
        } else if ((collisionBehavior == Globals.CollisionBehavior.ELASTIC || collisionBehavior == Globals.CollisionBehavior.FRAGMENT)
                && (otherBody.collisionBehavior == Globals.CollisionBehavior.ELASTIC ||
                otherBody.collisionBehavior == Globals.CollisionBehavior.FRAGMENT)) {
            CollisionCalcResult r = calcElasticCollision(otherBody);
            if (r.collided) {
                if (tryLock()) {
                    boolean otherLock = false;
                    try {
                        otherLock = otherBody.tryLock();
                        if (otherLock && exists && otherBody.exists) {
                            FragmentationCalcResult fr = shouldFragment(otherBody, r);
                            if (fr.shouldFragment) {
                                doFragment(otherBody, fr);
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
        vx = (float) ((r.vx1 - r.vx_cm) * R + r.vx_cm);
        vy = (float) ((r.vy1 - r.vy_cm) * R + r.vy_cm);
        vz = (float) ((r.vz1 - r.vz_cm) * R + r.vz_cm);
        otherBody.vx = (float) ((r.vx2 - r.vx_cm) * R + r.vx_cm);
        otherBody.vy = (float) ((r.vy2 - r.vy_cm) * R + r.vy_cm);
        otherBody.vz = (float) ((r.vz2 - r.vz_cm) * R + r.vz_cm);
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

        // commented this out from the original - if the radii overlap run the calc anyway because the sim doesn't
        // prevent bodies overlapping - that would take way too much compute power

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
            phi2 = Math.atan2(y2, x2);
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
        thetav = Math.acos(fvz1r);
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

        // commented out from original - position is assigned in the update method

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
    FragmentationCalcResult shouldFragment(Body otherBody, CollisionCalcResult r) {
        if (!(collisionBehavior == Globals.CollisionBehavior.FRAGMENT ||
                otherBody.collisionBehavior == Globals.CollisionBehavior.FRAGMENT)) {
            return FragmentationCalcResult.noFragmentation();
        }
        float vThis = vx + vy + vz;
        float dvThis = (float)
                (Math.abs(vx - ((r.vx1 - r.vx_cm) * R + r.vx_cm)) +
                Math.abs(vy - ((r.vy1 - r.vy_cm) * R + r.vy_cm)) +
                Math.abs(vz - ((r.vz1 - r.vz_cm) * R + r.vz_cm)));
        float vThisFactor = dvThis / Math.abs(vThis);
        float vOther = otherBody.vx + otherBody.vy + otherBody.vz;
        float dvOther = (float)
                (Math.abs(otherBody.vx - ((r.vx2 - r.vx_cm) * R + r.vx_cm)) +
                Math.abs(otherBody.vy - ((r.vy2 - r.vy_cm) * R + r.vy_cm)) +
                Math.abs(otherBody.vz - ((r.vz2 - r.vz_cm) * R + r.vz_cm)));
        float vOtherFactor = dvOther / Math.abs(vOther);
        //logger.log(CUSTOM, "this.id={} vThis=({},{},{})", id, vx, vy, vz);
        if ((collisionBehavior == Globals.CollisionBehavior.FRAGMENT && vThisFactor > fragFactor) ||
                (otherBody.collisionBehavior == Globals.CollisionBehavior.FRAGMENT && vOtherFactor > otherBody.fragFactor)) {
            return FragmentationCalcResult.fragmentation(vThisFactor, vOtherFactor);
        }
        return FragmentationCalcResult.noFragmentation();
    }

    /**
     * Handles a collision where one of the bodies is configured for fragmentation
     *
     * @param otherBody the other body being collided with
     * @param fr        results of fragmentation calc
     */
    private void doFragment(Body otherBody, FragmentationCalcResult fr) {
        if (collisionBehavior == Globals.CollisionBehavior.FRAGMENT && fr.thisFactor > fragFactor) {
            initiateFragmentation(fr.thisFactor);
        }
        if (otherBody.collisionBehavior == Globals.CollisionBehavior.FRAGMENT && fr.otherFactor > otherBody.fragFactor) {
            otherBody.initiateFragmentation(fr.otherFactor);
        }
    }

    /**
     * Calculates fragmentation params, creates a {@link FragInfo} instance to hold the values, and sets the
     * {@link #fragmenting} flag to true indicating that this body is now fragmenting
     *
     * @param fragFactor The calculated fragmentation factor based on velocity change for this body
     */
    private void initiateFragmentation(float fragFactor) {
        float fragDelta = fragFactor - this.fragFactor > 10 ? 10 : fragFactor - this.fragFactor;
        int fragments = Math.min((int) (fragDelta * fragmentationStep), MAX_FRAGS);
        if (fragments <= 1) {
            collisionBehavior = Globals.CollisionBehavior.ELASTIC;
            return;
        }
        fragmenting = true;
        SimpleVector curPos = new SimpleVector(x, y, z);
        float volume = (FOUR_THIRDS_PI * radius * radius * radius);
        float newRadius = (float) Math.max(Math.pow(((volume / fragments) * 3F) / FOUR_PI, 1F / 3F), .1F);
        float newMass = mass / fragments;
        fragInfo = new FragInfo(radius, newRadius, newMass, fragments, curPos);
    }

    /**
     * Fragments a body into smaller fragments as controlled by the {@link #fragInfo} field until the body has
     * been fully fragmented. Then sets this body to a fragment as well, and turns off fragmentation for the
     * instance. The number of fragments generated in any given cycle is capped by the {@link #MAX_FRAGS_PER_CYCLE}
     * constant so the simulation isn't held up while a large number of bodies are generated all at once in a case
     * where - say - one body fragments into a thousand
     *
     * @param bodyQueue to add bodies into
     */
    private void fragment(ConcurrentLinkedQueue<Body> bodyQueue) {
        int cnt = 0;
        while (fragInfo.fragments > 0) {
            --fragInfo.fragments;
            SimpleVector v = SimpleVector.getVectorEven(fragInfo.curPos, fragInfo.radius * .9F);
            bodyQueue.add(new Body(Body.nextID(), v.x, v.y, v.z, vx, vy, vz, fragInfo.mass, fragInfo.newRadius,
                    Globals.CollisionBehavior.ELASTIC, color, 0, 0, false, name, clas, false));
            if (++cnt >= MAX_FRAGS_PER_CYCLE) {
                break;
            }
        }
        if (fragInfo.fragments <= 0) {
            // turn this instance into a fragment
            mass = fragInfo.mass;
            radius = fragInfo.newRadius;
            collisionBehavior = Globals.CollisionBehavior.ELASTIC;
            fragmenting = false;
        } else {
            // shrink the body a little each time
            radius = radius * .9F;
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
