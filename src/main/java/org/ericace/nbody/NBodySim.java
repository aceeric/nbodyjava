package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Main class
 */
class NBodySim {
    private static final Logger logger = LogManager.getLogger(NBodySim.class);

    private static final int THREAD_COUNT = 2;
    private static final int MAX_RESULT_QUEUES = 20;
    private static final int BODY_COUNT = 3000;
    private static final double TIME_SCALING = .000000001F; // slows the simulation
    private static final double SOLAR_MASS = 1.98892e30;
    private static final boolean JME332Alpha = false;
    private static final String JME_THREADNAME = "jME3 Main";

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
    public static void main(String [] args) throws InterruptedException {
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
        if (JME332Alpha) { // have not been able to make this work yet...
            new Thread(jmeApp::start, JME_THREADNAME).start();
        } else {
            jmeApp.start();
        }
        Thread jmeThread = getJmeThread();
        if (jmeThread == null) {
            logger.error("Unable to find the JME thread");
            return;
        }
        ComputationRunner runner = new ComputationRunner(THREAD_COUNT, bodyQueue, TIME_SCALING, resultQueueHolder);
        new Thread(runner).start();
        jmeThread.join();
        runner.stopRunner();
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
     * @return the JME thread
     */
    private static Thread getJmeThread() {
        return Thread.getAllStackTraces().keySet()
                .stream()
                .filter(t -> t.getName().equals("jME3 Main")).findFirst().orElse(null);
    }

    /**
     * Creates a Queue and populates it with Body instances. This particular initializer creates
     * four clumps of bodies and initializes the velocity so the clumps will be captured by the
     * sun. Each clump contains mostly small similar sized bodies but a small number of larger
     * bodies are included for variety.
     *
     * @param bodyCount the number of bodies to place into the queue
     *
     * @return the Queue
     */
    private static ConcurrentLinkedQueue<Body> initBodyQueue(int bodyCount) {
        ConcurrentLinkedQueue<Body> bodyQueue = new ConcurrentLinkedQueue<>();
        int id = 0;
        double x, y, z, vx, vy, vz, radius, mass;
        double VCONST = 698000000D;
        for (int i = -1; i <= 1; i += 2) { // left/right
            for (int j = -1; j <= 1; j += 2) { // front/back
                for (int c = 0; c < bodyCount / 4; ++c) {
                    x = (.5 - Math.random()) * 420 + (400 * i);
                    y = (.5 - Math.random()) * 10;
                    z = (.5 - Math.random()) * 420 + (400 * j);
                    vy = .5 - Math.random(); // mostly in the same y plane
                    if      (i == -1 && j == -1) {vx = -VCONST; vz =  VCONST;}
                    else if (i == -1 && j ==  1) {vx =  VCONST; vz =  VCONST;}
                    else if (i ==  1 && j ==  1) {vx =  VCONST; vz = -VCONST;}
                    else                         {vx = -VCONST; vz = -VCONST;}
                    radius = c < bodyCount * .0025D ? 12 * Math.random() : 2D * Math.random(); // a few large bodies
                    mass = radius * SOLAR_MASS * .000005D;
                    bodyQueue.add(new Body(id++, x, y, z, vx, vy, vz, mass, (float) radius));
                }
            }
        }
        return bodyQueue;
    }
}
