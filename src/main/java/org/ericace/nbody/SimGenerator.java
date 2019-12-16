package org.ericace.nbody;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to generate lists of bodies to start the simulation with. Two methods are provided so far:
 * <p>
 *     {@link #defaultSim()} Sets up a canned simulation</p>
 * <p> 
 *     {@link #fromCSV(String)} Loads body definitions from a file</p>
 */
public class SimGenerator {
    private static final double SOLAR_MASS = 1.98892e30;

    /**
     * Creates a Queue and populates it with Body instances. This particular initializer creates
     * four clumps of bodies centered at "left, right, front, and back", with each clump organized
     * spherically, around that center point. The velocity of the bodies in each clump is set so
     * that each clump will be captured by the sun. Each clump contains mostly small, similar-sized
     * bodies but also a few larger bodies are included for variety.
     *
     * @return the Queue that was created and populated
     */
    static List<Body> defaultSim() {
        List<Body> bodies = new ArrayList<>();
        double vx, vy, vz, radius, mass;
        double V = 658000000D;
        int bodyCount = 2500;
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                double xc = 300 * i;
                double zc = 300 * j;
                for (int c = 0; c < bodyCount / 4; ++c) {
                    vy = .5 - Math.random(); // mostly in the same y plane
                    if      (i == -1 && j == -1) {vx = -V; vz =  V;}
                    else if (i == -1 && j ==  1) {vx =  V; vz =  V;}
                    else if (i ==  1 && j ==  1) {vx =  V; vz = -V;}
                    else                         {vx = -V; vz = -V;}
                    radius = c < bodyCount * .0025D ? 5.0 * Math.random() : 1.0 * Math.random();
                    mass = radius * SOLAR_MASS * .000005D;
                    Vector v = getVectorEven(new Vector((float) xc, 0.0F, (float) zc));
                    bodies.add(new Body(Body.nextID(), v.x, v.y, v.z, vx, vy, vz, mass, (float) radius));
                }
            }
        }
        createSunAndAddToQueue(bodies);
        return bodies;
    }

    /**
     * Parses a CSV into a list of bodies. The format must be comma-delimited, with fields:
     *
     * x,y,z,vz,vy,vz,mass,radius
     * ... or:
     * x,y,z,vz,vy,vz,mass,radius,true
     *
     * In the second form, the line specifies a sun. The first form specifies a non-sun body
     *
     * E.g.:
     * 100.0,100.0,100.0,100.0,100.0,100.0,10.0,.5
     * 1.0,1.0,1.0,1.0,1.0,1.0,10000.0,10,true
     *
     * The above example would load a simulation with one body and one sun
     *
     * @param pathSpec path of the CSV
     *
     * @return the parsed list of bodies
     */
    static List<Body> fromCSV(String pathSpec) {
        List<Body> bodies = new ArrayList<>();
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(pathSpec))) {
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                try {
                    double x = Double.parseDouble(fields[0]);
                    double y = Double.parseDouble(fields[1]);
                    double z = Double.parseDouble(fields[2]);
                    double vx = Double.parseDouble(fields[3]);
                    double vy = Double.parseDouble(fields[4]);
                    double vz = Double.parseDouble(fields[5]);
                    double mass = Double.parseDouble(fields[6]);
                    float radius = Float.parseFloat(fields[7]);
                    boolean isSun = fields.length == 9 && Boolean.parseBoolean(fields[8]);
                    Body b = new Body(Body.nextID(), x, y, z, vx, vy, vz, mass, radius);
                    if (isSun) {
                        b.setSun();
                    }
                    bodies.add(b);
                } catch (NumberFormatException e) {
                    // do nothing - load what is possible to load and ignore everything else
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse input file '" + pathSpec + "'");
        }
        return bodies;
    }

    /**
     * Creates a sun body with larger mass, very low (non-zero) velocity, placed at 0, 0, 0 and
     * places it into the passed body queue that holds the bodies in the simulation
     *
     * @param bodies a list of bodies in the simulation. The sun is appended
     */
    private static void createSunAndAddToQueue(List<Body> bodies) {
        double tmpRadius = 30;
        double tmpMass = tmpRadius * SOLAR_MASS * .1D;
        Body theSun = new Body(Body.nextID(), 0, 0, 0, -3, -3, -5, tmpMass, (float) tmpRadius);
        theSun.setSun();
        bodies.add(theSun);
    }

    /**
     * Generates a vector that is evenly distributed within a sphere around the vector defined in the
     * passed param. Meaning - if called multiple times, the result will be a set of vectors evenly
     * distributed within a sphere. This function is based on:
     *
     * https://karthikkaranth.me/blog/generating-random-points-in-a-sphere/
     *
     * @param center The Vector around which to center the generated vector
     *
     * @return a vector as as described
     */
    private static Vector getVectorEven(Vector center) {
        double d, x, y, z;
        do {
            x = Math.random() * 2.0 - 1.0;
            y = Math.random() * 2.0 - 1.0;
            z = Math.random() * 2.0 - 1.0;
            d = x*x + y*y + z*z;
        } while (d > 1.0);
        return new Vector((float) ((x * 100) + center.x), (float) ((y * 100) + center.y), (float) ((z * 100) + center.z));
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
    private static Vector getVectorConcentrated(Vector center) {
        double x = Math.random() - 0.5;
        double y = Math.random() - 0.5;
        double z = Math.random() - 0.5;
        double mag = Math.sqrt(x * x + y * y + z * z);
        x /= mag;
        y /= mag;
        z /= mag;
        double d = Math.random() * 200;
        return new Vector((float) ((x * d) + center.x), (float) ((y * d) + center.y), (float) ((z * d) + center.z));
    }
}
