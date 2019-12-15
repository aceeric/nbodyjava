package org.ericace.nbody;

import java.io.IOException;

/**
 * Entry point. Instantiates and runs the simulation class: {@link NBodySim}
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        new NBodySim().run(SimGenerator.defaultSim());
    }
}
