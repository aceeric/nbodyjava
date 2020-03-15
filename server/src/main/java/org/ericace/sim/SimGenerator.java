package org.ericace.sim;

import org.ericace.globals.Globals;
import org.ericace.nbody.Body;
import org.ericace.nbody.SimpleVector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Utility class that generates various canned simulations:
 *
 * <p>{@link #sim1}</p>
 * <p>{@link #sim2}</p>
 * <p>{@link #sim3}</p>
 * <p>{@link #sim4}</p>
 * <p>{@link #sim5}</p>
 *
 * <p>Also has a method to load a simulatiion from a CSV: {@link #fromCSV}</p>
 */
public class SimGenerator {
    private static final float SOLAR_MASS = 1.98892e30F;

    /**
     * Creates four clumps of bodies centered at "left, right, front, and back", with each clump organized
     * spherically, around that center point. The velocity of the bodies in each clump is set so
     * that each clump will be captured by the sun. Each clump contains mostly small, similar-sized
     * bodies but also a few larger bodies are included for variety.
     *
     * @param bodyCount         Max bodies
     * @param collisionBehavior The collision behavior for each body
     * @param defaultBodyColor  Default body color
     * @param simArgs           Unused by this generator
     *
     * @return a simulation instance containing a list of bodies
     */
    static Sim sim1(int bodyCount, Globals.CollisionBehavior collisionBehavior, Globals.Color defaultBodyColor,
                    String simArgs) {
        List<Body> bodies = new ArrayList<>();
        float vx, vy, vz, y, mass, radius, V = 958000000;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                float xc = 200 * i;
                float zc = 200 * j;
                Globals.Color color;
                if      (i == -1 && j == -1) {vx = -V; vz =  V; y = +100; color = defaultBodyColor == null ? Globals.Color.RED : defaultBodyColor;}
                else if (i == -1 && j ==  1) {vx =  V; vz =  V; y = -100; color = defaultBodyColor == null ? Globals.Color.YELLOW : defaultBodyColor;}
                else if (i ==  1 && j ==  1) {vx =  V; vz = -V; y = +100; color = defaultBodyColor == null ? Globals.Color.LIGHTGRAY : defaultBodyColor;}
                else                         {vx = -V; vz = -V; y = -100; color = defaultBodyColor == null ? Globals.Color.CYAN : defaultBodyColor;}
                for (int c = 0; c < bodyCount / 4; ++c) {
                    vy = .5F - (float) Math.random(); // mostly in the same y plane
                    float f = (float) Math.random();
                    radius = c < bodyCount * .0025 ? 8 * f : 3 * f;
                    mass = radius * SOLAR_MASS * .000005F;
                    SimpleVector v = SimpleVector.getVectorEven(new SimpleVector(xc, y, zc), 30);
                    bodies.add(new Body(Body.nextID(), v.x, v.y, v.z, vx, vy, vz, mass, radius, collisionBehavior,
                            color, 1, 1, false, null, null, false));
                }
            }
        }
        createSunAndAddToQueue(bodies, 0, 0, 0, 25F * SOLAR_MASS * .11F, 35);
        return new Sim(bodies, null);
    }

    /**
     * Generates a sun at 0,0,0 and a cluster of bodies off-screen headed for a very close pass around
     * with the sun at high velocity. Typically, a few bodies are captured by the sun but most travel away
     *
     * @param bodyCount         Max bodies
     * @param collisionBehavior The collision behavior for each body
     * @param defaultBodyColor  Default body color
     * @param simArgs           Unused by this generator
     *
     * @return a simulation instance containing a list of bodies
     */
    @SuppressWarnings("unused")
    static Sim sim2(int bodyCount, Globals.CollisionBehavior collisionBehavior, Globals.Color defaultBodyColor,
                    String simArgs) {
        List<Body> bodies = new ArrayList<>();
        createSunAndAddToQueue(bodies, 0, 0, 0, 25F * SOLAR_MASS * .1F, 25);
        for (int i = 0; i < bodyCount; ++i) {
            SimpleVector v = SimpleVector.getVectorEven(new SimpleVector(500.0F, 500.0F, 500.0F), 50);
            float mass = 2 * ((float) Math.random()) * SOLAR_MASS * .000005F;
            float radius = (float) Math.random() * 4;
            bodies.add(new Body(Body.nextID(), v.x, v.y, v.z, -1124500000F, -824500000F, -1124500000F, mass,
                    radius, collisionBehavior, defaultBodyColor, 1, 1, false, null, null, false));
        }
        return new Sim(bodies, null);
    }

    /**
     * Generates a simulation with a sun far removed from the focus area just to serve as light source. Creates
     * two clusters composed of many colliding spheres in close proximity. The two clusters exert gravitational
     * attraction toward each other as if they were solids. They also exert gravitational force within themselves,
     * preserving their spherical shape. The two clusters orbit and then collide, merging into a single cluster
     * of colliding spheres.
     *
     * <p>After the sim starts, the {@link SimThread} instance returned by the method injects a series of bodies
     * gradually into the simulation. The additional bodies come in over a period of a few minutes.</p>
     *
     * <p>This sim is dependent on body count - I run it with ~1000 bodies. Fewer, and the attraction isn't enough
     * to bring the clusters together. More, and the two clusters merge too soon. This sim should be run with
     * elastic collision. This example was useful to surface some subtleties with regard to how the simulation
     * handles lots of concurrent elastic collisions.</p>
     *
     * @param bodyCount         Max bodies
     * @param collisionBehavior The collision behavior for each body
     * @param defaultBodyColor  Default body color
     * @param simArgs           CSV in the form: radius of clump, mass of bodies in clump, body count to inject. E.g.:,
     *                          "70F,90E25F,500" (these are the defaults if no arg provided)
     *
     * @return a simulation instance containing a list of bodies and a {@link SimThread} instance for injecting
     * additional bodies after the sim starts.
     */
    @SuppressWarnings("unused")
    static Sim sim3(int bodyCount, Globals.CollisionBehavior collisionBehavior, Globals.Color defaultBodyColor,
                    String simArgs) {
        List<Body> bodies = new ArrayList<>();
        String [] parsedSimArgs = simArgs == null ? new String[] {"50F","90E25F","700"} : simArgs.split(",");
        float radius = parsedSimArgs.length >= 1 ? Float.parseFloat(parsedSimArgs[0]) : 70F;
        float mass = parsedSimArgs.length >= 2 ? Float.parseFloat(parsedSimArgs[1]) : 90E25F;
        int injectCnt = parsedSimArgs.length == 3 ? Integer.parseInt(parsedSimArgs[2]) : 500;
        // far away light source with minimal mass & gravity
        createSunAndAddToQueue(bodies, 100000, 100000, 100000, 1, 500);
        for (int j = -1; j <= 1; j += 2) {
            for (int i = 0; i < bodyCount / 2; ++i) {
                Globals.Color bodyColor = defaultBodyColor != null ? defaultBodyColor:
                    j == -1 ? Globals.Color.YELLOW : Globals.Color.RED;
                SimpleVector v = SimpleVector.getVectorEven(new SimpleVector(j * 70F, j * 70F, j * 70F), radius);
                bodies.add(new Body(Body.nextID(), v.x, v.y, v.z, j * 121185000, j * 121185000, j * -121185000,
                        mass, 5F, collisionBehavior, bodyColor, 1, 1, false, null, null, false));
            }
        }
        return new Sim(bodies, new sim3Thread(defaultBodyColor, collisionBehavior, injectCnt));
    }

    /**
     * Injects bodies into the sim3 body queue after the sim starts
     */
    private static class sim3Thread implements SimThread, Runnable {
        private boolean running = true;
        private ConcurrentLinkedQueue<Body> bodyQueue;
        private final Globals.CollisionBehavior collisionBehavior;
        private final Globals.Color defaultBodyColor;
        private final int injectCnt;

        public sim3Thread(Globals.Color defaultBodyColor, Globals.CollisionBehavior collisionBehavior, int injectCnt) {
            this.defaultBodyColor = defaultBodyColor;
            this.collisionBehavior = collisionBehavior;
            this.injectCnt = injectCnt;
        }
        @Override
        public void stop() {
            running = false;
        }

        @Override
        public void start(ConcurrentLinkedQueue<Body> bodyQueue) {
            this.bodyQueue = bodyQueue;
            new Thread(this).start();
        }
        @Override
        public void run() {
            int cnt = 0;
            while (running) {
                try {
                    if (cnt >= injectCnt) {
                        running = false;
                        System.out.println("Done: added " + injectCnt + " bodies");
                    } else {
                        ++cnt;
                        float x = (float) Math.random() * 5 - 200;
                        float y = (float) Math.random() * 5 + 400;
                        float z = (float) Math.random() * 5 - 200;
                        float radius = (float) Math.random() * 5;
                        float mass = radius * 2.93E+12F;
                        Globals.Color color = defaultBodyColor == null ? Globals.Color.BLUE : defaultBodyColor;
                        Body b = new Body(Body.nextID(), x, y, z, -99827312, 112344240, 323464000, mass, radius,
                                collisionBehavior, color, 1, 1, false, null, null, false);
                        bodyQueue.add(b);
                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    running = false;
                }
            }
        }
    }

    /**
     * Generates a sun and a line of bodies along the x axis all in the same plane moving at the same
     * velocity showing that nearer objects are captured more quickly than farther objects
     *
     * @param bodyCount         Max bodies
     * @param collisionBehavior The collision behavior for each body
     * @param defaultBodyColor  Default body color
     * @param simArgs           The number of additional bodies to inject after the sim starts. Defaults to 700
     *
     * @return a simulation instance containing a list of bodies and a {@link SimThread} instance for modifying
     * the collision behavior of the planet after the collision with the impactor occurs.
     */
    @SuppressWarnings("unused")
    static Sim sim4(int bodyCount, Globals.CollisionBehavior collisionBehavior, Globals.Color defaultBodyColor,
                    String simArgs) {
        List<Body> bodies = new ArrayList<>();
        createSunAndAddToQueue(bodies, 0, 0, 0, SOLAR_MASS, 30);
        for (int i = 0; i < bodyCount; ++i) {
            float mass = 9e5F;
            float radius = 2;
            bodies.add(new Body(Body.nextID(), (i*4) + 100, 0, 0, // x,y,z
                    0, 0, -824500000 + (i * 1E6F), // vx, vy, vz
                    mass, radius, collisionBehavior, defaultBodyColor, 1, 1, false, null, null, false));
        }
        return new Sim(bodies, null);
    }

    /**
     * Generates a sun far removed from the focus area just to serve as light source. Creates a large
     * planet at the center of the sim orbited by two moons moon. Creates a small impactor headed for
     * the large planet. The impactor is configured to fragment into many smaller bodies on impact.
     *
     * <p>After the sim starts, the {@link SimThread} instance returned by the method monitors the simulation
     * and when the impact occurs it changes the planet's collision behavior from ELASTIC to SUBSUME. As a
     * result, any of the smaller fragments that subsequently strike the planet are absorbed into the planet.</p>
     *
     * @param bodyCount         Ignored
     * @param collisionBehavior Ignored
     * @param defaultBodyColor  Ignored
     * @param simArgs           CSV configuring the impactor in the form frag factor,frag step. E.g.:
     *                          .01F,1000 (the default). The larger the factor, the more force is required to
     *                          cause fragmentation. The larger the step, the more fragments are created.
     *
     * @return a simulation instance containing a list of bodies
     **/
     @SuppressWarnings("unused")
    static Sim sim5(int bodyCount, Globals.CollisionBehavior collisionBehavior, Globals.Color defaultBodyColor,
                    String simArgs) {
        List<Body> bodies = new ArrayList<>();
        String [] parsedSimArgs = simArgs == null ? new String[] {".01F","1000"} : simArgs.split(",");
        float fragFactor = parsedSimArgs.length >= 1 ? Float.parseFloat(parsedSimArgs[0]) : .01F;
        float fragStep = parsedSimArgs.length >= 2 ? Float.parseFloat(parsedSimArgs[1]) : 1000;
        // far away light source with minimal mass & gravity
        createSunAndAddToQueue(bodies, 100000, 100000, 100000, 1, 500);
        // planet
        bodies.add(new Body(Body.nextID(), 0, 0, 0, 12, 12, 12, 9E30F, 145F, Globals.CollisionBehavior.ELASTIC,
                Globals.Color.RED, 0, 0, false, null, null, false));
        // moons
        bodies.add(new Body(Body.nextID(), 50, 0, -420, -980000000, 12, -500000000, 9E20F, 35F,
                Globals.CollisionBehavior.SUBSUME, Globals.Color.LIGHTGRAY, 0, 0, false, null, null, false));
        bodies.add(new Body(Body.nextID(), -400, 50, 405, 530000000, -313000000, 520000000, 9E19F, 5F,
                Globals.CollisionBehavior.ELASTIC, Globals.Color.CYAN, 0, 0, false, null, null, false));
        bodies.add(new Body(Body.nextID(), 70, 0, -520, -880000000, -10000, -300000000 , 11E22F, 15F,
                Globals.CollisionBehavior.ELASTIC, Globals.Color.DARKGRAY, 0, 0, false, null, null, false));

        // impactor
        bodies.add(new Body(Body.nextID(), 900, -900, 900, -450000000, 723000000, -350000000, 9E12F, 10F,
                Globals.CollisionBehavior.FRAGMENT, Globals.Color.YELLOW, fragFactor, fragStep, false, null, null, false));
        return new Sim(bodies, new sim5Thread());
    }

    /**
     * Changes the planet from ELASTIC to SUBSUME after the impactor collides so the planet absorbs
     * the fragments if they subsequently impact the planet again
     */
    private static class sim5Thread implements SimThread, Runnable {
        private boolean running = true;
        private ConcurrentLinkedQueue<Body> bodyQueue;
        private Body planet;

        @Override
        public void stop() {
            running = false;
        }

        @Override
        public void start(ConcurrentLinkedQueue<Body> bodyQueue) {
            this.bodyQueue = bodyQueue;
            for (Body b : bodyQueue) {
                if (b.getId() == 1) {
                    planet = b;
                    break;
                }
            }
            new Thread(this).start();
        }
        @Override
        public void run() {
            while (running) {
                try {
                    if (bodyQueue.size() > 6) {
                        planet.setCollisionBehavior(Globals.CollisionBehavior.SUBSUME);
                        running = false;
                    } else {
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    running = false;
                }
            }
        }
    }

    /**
     * Parses a CSV file into a list of bodies. The format must be comma-delimited, with fields:
     *
     * x,y,z,vx,vy,vz,mass,radius,is_sun,collision_behavior,color,fragmentation_factor,fragmentation_step
     *
     * Everything from 'x' through 'radius' is required - and is parsed as a float. Everything else is optional.
     * Comments are allowed: any line where '#' is the first character
     *
     * If 'is_sun' is omitted, the loader defaults it to 'False' The following values are parsed as TRUE:
     * 'true', 'T', 1, 'yes', 'y'. Anything else is parsed as FALSE. (E.g. 'potato'). Any case is allowed.
     * E.g. FALSE, false, False, FaLsE, StringBean, etc.
     *
     * The following values are allowed for collision_behavior, also in any case: none, elastic, subsume, fragment.
     * If no value is provided, then 'elastic' is defaulted.
     *
     * Refer to the {@link Globals.Color} enum for color values. They can be provided in any case. If not provided,
     * a random color is selected for each body in the CSV
     *
     * Example:
     * 100,100,100,100,100,100,10,.5,,,blue
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
    static List<Body> fromCSV(String pathSpec, int bodyCount, Globals.CollisionBehavior defaultCollisionBehavior,
                              Globals.Color defaultBodyColor) {
        List<Body> bodies = new ArrayList<>();
        String line;
        int lines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(pathSpec))) {
            while ((line = br.readLine()) != null && ++lines <= bodyCount) {
                if (line.startsWith("#")) continue;
                String[] fields = line.split(",");
                try {
                    float x = Float.parseFloat(fields[0].trim());
                    float y = Float.parseFloat(fields[1].trim());
                    float z = Float.parseFloat(fields[2].trim());
                    float vx = Float.parseFloat(fields[3].trim());
                    float vy = Float.parseFloat(fields[4].trim());
                    float vz = Float.parseFloat(fields[5].trim());
                    float mass = Float.parseFloat(fields[6].trim());
                    float radius = Float.parseFloat(fields[7].trim());
                    boolean isSun = fields.length >= 9 && Globals.parseBoolean(fields[8].trim());
                    Globals.CollisionBehavior collisionBehavior = fields.length >= 10 ?
                            Globals.parseCollisionBehavior(fields[9].trim()) : defaultCollisionBehavior;
                    Globals.Color color = fields.length >= 11 ? Globals.parseColor(fields[10].trim()) : defaultBodyColor;
                    float fragFactor = fields.length >= 12 ? Float.parseFloat(fields[12].trim()) : 1;
                    float fragStep = fields.length >= 13 ? Float.parseFloat(fields[13].trim()) : 1;
                    Body b = new Body(Body.nextID(), x, y, z, vx, vy, vz, mass, radius, collisionBehavior, color,
                            fragFactor, fragStep, false, null, null, false);
                    if (isSun) {
                        b.setSun();
                    }
                    bodies.add(b);
                } catch (NumberFormatException e) {
                    if (!line.replaceAll("\\s+","").toLowerCase().startsWith("x,y,z,vx,vy,vz")) {
                        // load what is possible to load and ignore everything else
                        System.out.println("Ignoring line: " + line);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse input file '" + pathSpec + "'");
        }
        return bodies;
    }

    /**
     * Creates a sun body with larger mass, very low (non-zero) velocity, placed at 0, 0, 0 and
     * places it into the passed body list
     *
     * @param bodies a list of bodies in the simulation. The sun is appended to the list
     */
    private static void createSunAndAddToQueue(List<Body> bodies, float x, float y, float z, float mass, float radius) {
        Body theSun = new Body(Body.nextID(), x, y, z, -3, -3, -5, mass, radius, Globals.CollisionBehavior.SUBSUME,
                null, 1, 1, false, null, null, false);
        theSun.setSun();
        bodies.add(theSun);
    }
}
