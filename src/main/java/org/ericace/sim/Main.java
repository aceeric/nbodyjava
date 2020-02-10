package org.ericace.sim;

import org.ericace.nbody.Body;
import org.ericace.nbody.SimpleVector;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Main class
 */
public class Main {

    private static final String DEFAULT_SIM_NAME = "sim1";

    private static boolean render = true;
    private static int threads = 5;
    private static double scaling = .000000001D;
    private static String simName = null;
    private static Body.CollisionBehavior defaultCollisionBehavior = Body.CollisionBehavior.ELASTIC;
    private static int bodyCount = 1000;
    private static String csvPath = null;
    private static Body.Color defaultBodyColor = null;
    private static SimpleVector initialCam = new SimpleVector(-100, 300, 1200);
    private static String simArgs = null;

    /**
     * Entry point. Instantiates and runs the simulation class: {@link NBodySim}. Parses args to set params,
     * and select the simulation to run. Can load a simulation from a CSV. See the {@link SimGenerator#fromCSV}
     * method for the expected CSV format
     *
     * If no command-line arg is provided then a default (built-in) simulation is run, as defined by
     * {@link SimGenerator#sim1}.
     *
     * @param args command-line args
     */
    public static void main(String[] args) {
        if (!parseArgs(args)) {
            return;
        }
        Sim t;
        if (csvPath != null) {
            List<Body> bodies = SimGenerator.fromCSV(csvPath, bodyCount, defaultCollisionBehavior, defaultBodyColor);
            t = new Sim(bodies, null);
        } else {
            Method method = getSimMethodFor(simName);
            if (method == null) {
                System.out.println("ERROR: Unknown sim specified on the command line: " + simName);
                return;
            }
            try {
                t = (Sim) method.invoke(null, bodyCount, defaultCollisionBehavior, defaultBodyColor, simArgs);
            } catch (Exception e) {
                System.out.println("ERROR: Could not generate sim: " + simName);
                return;
            }
        }
        new NBodySim().run(t.bodies, threads, scaling, initialCam, t.thread);
    }

    /**
     * Gets the simulation method from the {@link SimGenerator} class matching the passed name
     *
     * @param simName the sim method name
     *
     * @return the sim method, or null if no matching method
     */
    private static Method getSimMethodFor(String simName) {
        for (Method method : SimGenerator.class.getDeclaredMethods()) {
            if (method.getName().equals(simName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * A very rudimentary command-line option parser. Accepts short-form opts like -t and long-form like --threads.
     * Accepts this form: -t 1 and --threads 1, as well as this form -t=1 and --threads=1. Does not accept
     * concatenated short form opts in cases where such opts don't accept params. E.g. doesn't handle: -ot=1 where
     * -o is a parameterless option, and -t takes a value (one in this example.) Doesn't have any error handling
     * so - is fragile with respect to parsing errors.
     *
     * Sets class static fields corresponding to command line args.
     *
     * @param args command-line args
     *
     * @return False if there was an arg parse error, else return True
     */
    private static boolean parseArgs(String[] args) {
        Queue<String> argQueue = new LinkedList<>();
        for (String arg : args) {
            String [] s = arg.split("=");
            argQueue.add(s[0]);
            if (s.length == 2) {
                argQueue.add(s[1]);
            }
        }
        String arg;
        while ((arg = argQueue.poll()) != null) {
            try {
                switch (arg.toLowerCase()) {
                    case "-r":
                    case "--no-render":
                        render = false;
                        break;
                    case "-n":
                    case "--sim-name":
                        simName = argQueue.poll();
                        break;
                    case "-a":
                    case "--sim-args":
                        simArgs = argQueue.poll();
                        break;
                    case "-c":
                    case "--collision":
                        defaultCollisionBehavior = SimGenerator.parseCollisionBehavior(argQueue.poll());
                        break;
                    case "-b":
                    case "--bodies":
                        bodyCount = Integer.parseInt(argQueue.poll());
                        break;
                    case "-t":
                    case "--threads":
                        threads = Integer.parseInt(argQueue.poll());
                        break;
                    case "-m":
                    case "--scaling":
                        scaling = Double.parseDouble(argQueue.poll());
                        break;
                    case "-f":
                    case "--csv":
                        csvPath = argQueue.poll();
                        break;
                    case "-l":
                    case "--body-color":
                        defaultBodyColor = SimGenerator.parseColor(argQueue.poll());
                        break;
                    case "-i":
                    case "--initial-cam":
                        initialCam = parseVector(argQueue.poll());
                        break;
                    default:
                        System.out.println("ERROR: unknown option: " + arg);
                        return false;
                }
            } catch (Exception e) {
                System.out.println("ERROR parsing the command line: " + e.getMessage());
                return false;
            }
        }
        if (simName != null && csvPath != null) {
            System.out.println("ERROR: provide *either* a sim name *or* a csv path, but not both");
            return false;
        }
        if (simName == null && csvPath == null) {
            simName = DEFAULT_SIM_NAME;
        }
        return true;
    }

    /**
     * Parses a string like "1,2,3" into a SimpleVector with values x=1, y=2, z=3
     *
     * @param s The string to parse
     *
     * @return the vector from the components of the passed string, or null if the number of components is
     * not three
     */
    private static SimpleVector parseVector(String s) {
        String [] components = s.split(",");
        if (components.length != 3) {
            throw new RuntimeException("Invalid vector format: " + s);
        }
        return new SimpleVector(Float.parseFloat(components[0].trim()), Float.parseFloat(components[1].trim()),
                Float.parseFloat(components[2].trim()));
    }
}
