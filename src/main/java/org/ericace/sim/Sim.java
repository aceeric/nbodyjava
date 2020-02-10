package org.ericace.sim;

import org.ericace.nbody.Body;

import java.util.List;

/**
 * The Sim class holds a list of bodies to start a simulation with and - optionally - a {@link SimThread} reference
 * that allows the simulation body queue to be manipulated concurrently while the simulation is running. The use
 * case is - an initial sim state is defined in the list of bodies, and then once the sim runner starts the simulation,
 * it calls the {@code start} method on the {@code SimThread} member which can then add/remove bodies to/from
 * the body queue.
 *
 * <p>This is a simple value class so - no getters</p>
 */
public class Sim {
    final List<Body> bodies;
    final SimThread thread;

    /**
     * Constructor
     *
     * @param bodies a List of Body instances
     * @param thread An instance that wants to alter the body queue. The {@code start} method will be called by
     *               the sim runner once all the Body instances have been added to the body queue and the sim
     *               started
     */
    Sim(List<Body> bodies, SimThread thread) {
        this.bodies = bodies;
        this.thread = thread;
    }
}
