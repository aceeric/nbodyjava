package org.ericace.sim;

import org.ericace.nbody.Body;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Implementers of this interface can inject bodies into the simulation while it is running
 */
public interface SimThread {
    /**
     * Stops the running thread
     */
    void stop();

    /**
     * Starts a thread
     *
     * @param bodyQueue the queue of bodies comprising the simulation. The thread can modify the simulation
     *                  by, for example, adding bodies to the queue
     */
    void start(ConcurrentLinkedQueue<Body> bodyQueue);
}
