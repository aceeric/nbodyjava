package org.ericace.nbody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Main class
 */
class NBodySim {
    private static final int THREAD_COUNT = 2;
    private static final int MAX_RESULT_QUEUES = 50;
    private static final int BODY_COUNT = 2000;
    private static final double TIME_SCALING = .000000001F; // slows the simulation
    private static final double SOLAR_MASS = 1.98892e30;

    /**
     * <ol>
     *     <li>Initializes a queue to hold all the bodies in the simulation</li>
     *     <li>Fills the queue with bodies</li>
     *     <li>Initializes a result queue holder to hold computed results</li>
     *     <li>Initializes a computation runner and starts it which perpetually computes the body forces in a thread,
     *         placing the computed results into the result queue holder</li>
     *     <li>Initializes a JMonkey App and starts it which renders the computed results from the result queue
     *         perpetually in a thread</li>
     *     <li>Cleans up on exit</li>
     * </ol>
     */
    public static void main(String [] args) throws IOException, InterruptedException {
        ConcurrentLinkedQueue<Body> bodyQueue = initBodyQueue(BODY_COUNT);
        ResultQueueHolder resultQueueHolder = new ResultQueueHolder(MAX_RESULT_QUEUES);
        ArrayList<BodyRenderInfo> bodies = new ArrayList<>(bodyQueue.size());
        for (Body body : bodyQueue) {
            bodies.add(body.getRenderInfo());
        }
        Body sun = createSun(bodyQueue.size());
        bodyQueue.add(sun);
        BodyRenderInfo ri = sun.getRenderInfo();
        ri.setSun();
        bodies.add(ri);

        JMEApp jmeApp = new JMEApp(bodies, resultQueueHolder, new Vector(-100, 300, 800));
        jmeApp.start();
        ComputationRunner runner = new ComputationRunner(THREAD_COUNT, bodyQueue, TIME_SCALING, resultQueueHolder);
        new Thread(runner).start();
        // TODO pick up JME ESC and stop the runner
    }

    /**
     * Creates a sun body with larger mass, very low velocity, placed at 0, 0, 0
     *
     * @param id the ID to assign
     *
     * @return the Body
     */
    private static Body createSun(int id) {
        double tmpRadius = 30;
        double tmpMass = tmpRadius * SOLAR_MASS * .1D;
        return new Body(id, 0, 0, 0, -3, -3, -5, tmpMass, (float) tmpRadius);
    }

    /**
     * Creates a Queue and populates it with Body instances. This particular initializer creates
     * two clumps of bodies, one on the right, and one on the left, and initializes the velocity so
     * the two clumps will be captured by the sun. Each clump contains mostly small similar sized
     * bodies but a small number of larger bodies are included for variety.
     *
     * @param bodyCount the number of bodies to place into the queue
     *
     * @return the Queue
     */
    private static ConcurrentLinkedQueue<Body> initBodyQueue(int bodyCount) {
        ConcurrentLinkedQueue<Body> bodyQueue = new ConcurrentLinkedQueue<>();
        int id = 0;
        for (int i = -1; i <= 1; i += 2) { // left clump/right clump
            for (int j = 0; j < bodyCount / 2; ++j) {
                double x = ((.5 - Math.random()) * 420) + (400 * i);
                double y = (.5 - Math.random()) * 10;
                double z = (.5 - Math.random()) * 490;
                double vx = 0;
                double vy = (.5 - Math.random()) / 2; // mostly in the same y plane
                double vz = -1100000000D * i;
                double radius = j < 5 ? 12 * Math.random() : 2 * Math.random(); // a few large bodies in each clump
                double mass = radius * SOLAR_MASS * .000005;
                bodyQueue.add(new Body(id++, x, y, z, vx, vy, vz, mass, (float) radius));
            }
        }
        return bodyQueue;
    }
}