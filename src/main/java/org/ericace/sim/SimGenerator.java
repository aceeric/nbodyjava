package org.ericace.sim;

import org.ericace.nbody.Body;
import org.ericace.nbody.SimpleVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Utility class to generate lists of bodies to start the simulation with. The following methods are provided:
 *
 * <p>{@link #defaultSim} -- A canned simulation</p>
 * <p>{@link #sim2}       -- Another canned simulation</p>
 * <p>{@link #sim3}       -- Another canned simulation</p>
 * <p>{@link #fromCSV}    -- Loads body definitions from a CSV file</p>
 */
public class SimGenerator {
    private static final double SOLAR_MASS = 1.98892e30;

    /**
     * defines CSV values to parse as True
     */
    private static final List<String> TRUES = Arrays.asList("t", "true", "1", "y", "yes");

    /**
     * Creates a Queue and populates it with Body instances. This particular initializer creates
     * four clumps of bodies centered at "left, right, front, and back", with each clump organized
     * spherically, around that center point. The velocity of the bodies in each clump is set so
     * that each clump will be captured by the sun. Each clump contains mostly small, similar-sized
     * bodies but also a few larger bodies are included for variety.
     *
     * @param bodyCount         Max bodies
     * @param collisionBehavior The collision behavior for each body
     *
     * @return the Queue that was created and populated
     */
    static List<Body> defaultSim(int bodyCount, Body.CollisionBehavior collisionBehavior, Body.Color defaultBodyColor) {
        List<Body> bodies = new ArrayList<>();
        double vx, vy, vz, radius, mass;
        double V = 458000000D;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                double xc = 200 * i;
                double zc = 200 * j;
                for (int c = 0; c < bodyCount / 4; ++c) {
                    vy = .5 - Math.random(); // mostly in the same y plane
                    if      (i == -1 && j == -1) {vx = -V; vz =  V;}
                    else if (i == -1 && j ==  1) {vx =  V; vz =  V;}
                    else if (i ==  1 && j ==  1) {vx =  V; vz = -V;}
                    else                         {vx = -V; vz = -V;}
                    radius = c < bodyCount * .0025D ? 5.0 * Math.random() : 1.0 * Math.random();
                    mass = radius * SOLAR_MASS * .000005D;
                    SimpleVector v = getVectorEven(new SimpleVector((float) xc, 0.0F, (float) zc), 30);
                    //SimpleVector v = getVectorOnSphere(new SimpleVector((float) xc, 0.0F, (float) zc), 30);
                    bodies.add(new Body(Body.nextID(), v.x, v.y, v.z, vx, vy, vz, mass, (float) radius,
                            collisionBehavior, defaultBodyColor));
                }
            }
        }
        createSunAndAddToQueue(bodies, 0, 0, 0, 25 * SOLAR_MASS * .1D, 25);
        return bodies;
    }

    /**
     * Generates a body queue with sun at 0,0,0 and a cluster of bodies off-screen headed for a very close pass around
     * with the sun at high velocity
     *
     * @param bodyCount         Max bodies
     * @param collisionBehavior The collision behavior for each body
     *
     * @return the Queue that was created and populated
     */
    static List<Body> sim2(int bodyCount, Body.CollisionBehavior collisionBehavior, Body.Color defaultBodyColor) {
        List<Body> bodies = new ArrayList<>();
        createSunAndAddToQueue(bodies, 0, 0, 0, 25 * SOLAR_MASS * .1D, 25);
        for (int i = 0; i < bodyCount; ++i) {
            SimpleVector v = getVectorEven(new SimpleVector(500.0F, 500.0F, 500.0F), 50);
            double mass = 2 * Math.random() * SOLAR_MASS * .000005D;
            double radius = Math.random() * 4;
            bodies.add(new Body(Body.nextID(), v.x, v.y, v.z, -1124500000D, -824500000D, -1124500000D, mass,
                    (float) radius, collisionBehavior, defaultBodyColor));
        }
        return bodies;
    }

    /**
     * Generates a simulation with a sun far removed from the focus area just to serve as light source. Creates
     * two clusters composed of many colliding spheres in close proximity. The two clusters exert gravitational
     * attraction toward each other as if they were solids. They also exert gravitational force within themselves,
     * preserving their spherical shape. The two clusters orbit a couple times then collide, merging into a single
     * cluster of colliding spheres. This sim is dependent on body count - I run it with ~555 bodies. Fewer, and
     * the attraction isn't enough to bring the clusters together. More, and the two clusters quickly merge. This
     * sim should be run with elastic collision. This example was useful to surface some subtleties with regard to
     * how the simulation handles lots of concurrent collisions.
     *
     * @param bodyCount         Max bodies
     * @param collisionBehavior The collision behavior for each body
     *
     * @return the Queue that was created and populated
     */
    static List<Body> sim3(int bodyCount, Body.CollisionBehavior collisionBehavior, Body.Color defaultBodyColor) {
        List<Body> bodies = new ArrayList<>();
        createSunAndAddToQueue(bodies, 100000, 100000, 100000, 1, 500); // far away a light source minimal grav
        for (int i = 0; i < bodyCount; ++i) {
            for (float j = -1; j <= 1; j += 2) {
                Body.Color bodyColor = defaultBodyColor != null ? defaultBodyColor:
                    j == -1 ? Body.Color.YELLOW : Body.Color.RED;
                SimpleVector v = getVectorEven(new SimpleVector(j * 70F, j * 70F, j * 70F), 50);
                bodies.add(new Body(Body.nextID(), v.x, v.y, v.z, j * 121185000, j * 121185000, j * -121185000,
                        90E25, 5F, collisionBehavior, bodyColor));
            }
        }
        return bodies;
    }


    /**
     * Parses a CSV file into a list of bodies. The format must be comma-delimited, with fields:
     *
     * x,y,z,vx,vy,vz,mass,radius,is_sun,collision_behavior,color
     *
     * Everything from 'x' through 'radius' is required - and is parsed as a double.
     *
     * If 'is_sun' is omitted, the loader defaults it to 'False' The following values are parsed as TRUE:
     * 'true', 'T', 1, 'yes', 'y'. Anything else is parsed as FALSE. (E.g. 'potato'). Any case is allowed.
     * E.g. FALSE, false, False, FaLsE, StringBean, etc.
     *
     * The following values are allowed for collision_behavior, also in any case: none, elastic, subsume, fragment.
     * If no value is provided, then 'elastic' is defaulted.
     *
     * Refer to the {@link Body.Color} enum for color values. They can be provided in any case. If not provided,
     * a random color is selected
     *
     * Example:
     * 100,100,100,100,100,100,10,.5
     * 1,1,1,1,1,1,10000,10,true,elastic
     *
     * The above example would load a simulation with one non-sun body, and one sun, both with elastic collision
     * behavior.
     *
     * @param pathSpec                 Path of the CSV
     * @param bodyCount                Max number of bodies to read from the CSV. To guarantee inclusion of
     *                                 a body from the CSV, place it earlier in the file than this value.
     * @param defaultCollisionBehavior The default collision behavior for each body, if not specified in the
     *                                 CSV
     *
     * @return the parsed list of bodies
     */
    static List<Body> fromCSV(String pathSpec, int bodyCount, Body.CollisionBehavior defaultCollisionBehavior,
                              Body.Color defaultBodyColor) {
        List<Body> bodies = new ArrayList<>();
        String line;
        int lines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(pathSpec))) {
            while ((line = br.readLine()) != null && ++lines <= bodyCount) {
                String[] fields = line.split(",");
                try {
                    double x = Double.parseDouble(fields[0].trim());
                    double y = Double.parseDouble(fields[1].trim());
                    double z = Double.parseDouble(fields[2].trim());
                    double vx = Double.parseDouble(fields[3].trim());
                    double vy = Double.parseDouble(fields[4].trim());
                    double vz = Double.parseDouble(fields[5].trim());
                    double mass = Double.parseDouble(fields[6].trim());
                    float radius = Float.parseFloat(fields[7].trim());
                    boolean isSun = fields.length >= 9 && parseBoolean(fields[8].trim());
                    Body.CollisionBehavior collisionBehavior = fields.length >= 10 ?
                            parseCollisionBehavior(fields[9].trim()) : defaultCollisionBehavior;
                    Body.Color color = fields.length >= 11 ? parseColor(fields[10].trim()) : defaultBodyColor;
                    Body b = new Body(Body.nextID(), x, y, z, vx, vy, vz, mass, radius, collisionBehavior, color);
                    if (isSun) {
                        b.setSun();
                    }
                    bodies.add(b);
                } catch (NumberFormatException e) {
                    // load what is possible to load and ignore everything else
                    System.out.println("Ignoring line: " + line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse input file '" + pathSpec + "'");
        }
        return bodies;
    }

    /**
     * @return the passed string parsed as a boolean, as defined by the {@link #TRUES} constant. Null
     * parses as False.
     */
    static boolean parseBoolean(String s) {
        return s != null && TRUES.contains(s.toLowerCase());
    }

    /**
     * @return the passed string as a {@link Body.CollisionBehavior} enum. Valid values are "elastic", "none",
     * "fragment", and "subsume" (in any case) as defined by the referenced enum. Null parses as
     * Body.CollisionBehavior.ELASTIC.
     */
    static Body.CollisionBehavior parseCollisionBehavior(String s) {
        return s != null ? Body.CollisionBehavior.valueOf(s.toUpperCase()) : Body.CollisionBehavior.ELASTIC;
    }

    /**
     * @return the passed string as a {@link Body.Color} enum. Null parses as Body.Color.RANDOM
     */
    static Body.Color parseColor(String s) {
        return s != null ? Body.Color.valueOf(s.toUpperCase()) : Body.Color.RANDOM;
    }

    /**
     * Creates a sun body with larger mass, very low (non-zero) velocity, placed at 0, 0, 0 and
     * places it into the passed body queue
     *
     * @param bodies a list of bodies in the simulation. The sun is appended to the list
     */
    private static void createSunAndAddToQueue(List<Body> bodies, double x, double y, double z, double mass,
                                               double radius) {
        Body theSun = new Body(Body.nextID(), x, y, z, -3, -3, -5, mass, (float) radius, Body.CollisionBehavior.SUBSUME,
                null);
        theSun.setSun();
        bodies.add(theSun);
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
    private static SimpleVector getVectorEven(SimpleVector center, double radius) {
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
    private static SimpleVector getVectorConcentrated(SimpleVector center) {
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
    private static SimpleVector getVectorOnSphere(SimpleVector center, double radius) {
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
