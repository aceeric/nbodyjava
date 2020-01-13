package org.ericace.sim;

import org.ericace.nbody.Body;

import java.util.List;

/**
 * Entry point. Instantiates and runs the simulation class: {@link NBodySim}. If an arg is provided
 * then it is assumed to be the path of a CSV file. The CSV file is loaded and used to run the simulation.
 * If no command-line arg is provided then a default (built-in) simulation is run, as defined by
 * {@link SimGenerator#defaultSim()}.
 *
 * See the {@link SimGenerator#fromCSV} method for the expected CSV format
 */
public class Main {
    public static void main(String[] args) {
        List<Body> bodies = args.length == 1 ? SimGenerator.fromCSV(args[0]) : SimGenerator.defaultSim();
        new NBodySim().run(bodies);
    }
}