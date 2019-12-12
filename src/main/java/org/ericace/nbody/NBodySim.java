package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ericace.grpcserver.Configurables;
import org.ericace.grpcserver.NBodyServiceServer;
import org.ericace.instrumentation.Instrumentation;
import org.ericace.instrumentation.InstrumentationManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Main class
 */
class NBodySim {
    private static final Logger logger = LogManager.getLogger(NBodySim.class);

    private static final int DEFAULT_THREAD_COUNT = 3;
    private static final int DEFAULT_MAX_RESULT_QUEUES = 10;
    private static final int DEFAULT_BODY_COUNT = 500;
    private static final double DEFAULT_TIME_SCALING = .000000001F; // slows the simulation
    private static final double SOLAR_MASS = 1.98892e30;
    private static final String JME_THREAD_NAME = "jME3 Main";
    private static final Instrumentation inst = InstrumentationManager.getInstrumentation();

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
        ConcurrentLinkedQueue<Body> bodyQueue = initBodyQueue(DEFAULT_BODY_COUNT);
        ResultQueueHolder resultQueueHolder = new ResultQueueHolder(DEFAULT_MAX_RESULT_QUEUES);
        ArrayList<BodyRenderInfo> bodies = new ArrayList<>(bodyQueue.size());
        for (Body body : bodyQueue) {
            bodies.add(body.getRenderInfo());
        }
        Body sun = createSun();
        int idOfSun  = sun.getId();
        bodyQueue.add(sun);
        BodyRenderInfo ri = sun.getRenderInfo();
        ri.setSun();
        bodies.add(ri);

        JMEApp jmeApp = new JMEApp(bodies, resultQueueHolder, new Vector(-100, 300, 1200));
        jmeApp.start();
        Thread jmeThread = getJmeThread();
        if (jmeThread == null) {
            logger.error("Unable to find the JME thread");
            return;
        }
        ComputationRunner computationRunner = new ComputationRunner(DEFAULT_THREAD_COUNT, bodyQueue,
                DEFAULT_TIME_SCALING, resultQueueHolder);
        new Thread(computationRunner).start();
        NBodyServiceServer gRPCServer = new NBodyServiceServer(new ConfigurablesImpl(bodyQueue, resultQueueHolder,
                computationRunner, idOfSun));
        gRPCServer.start();
        jmeThread.join(); // ESC key is handled by JME and terminates the render thread
        computationRunner.stopRunner();
        gRPCServer.stop();
        inst.stop();
        logger.info("Exiting the simulation");
    }

    /**
     * Creates a sun body with larger mass, very low velocity, placed at 0, 0, 0
     *
     * @return the Body
     */
    private static Body createSun() {
        double tmpRadius = 30;
        double tmpMass = tmpRadius * SOLAR_MASS * .1D;
        return new Body(Body.nextID(), 0, 0, 0, -3, -3, -5, tmpMass, (float) tmpRadius);
    }

    /**
     * @return the JME thread
     */
    private static Thread getJmeThread() {
        return Thread.getAllStackTraces().keySet()
                .stream()
                .filter(t -> t.getName().equals(JME_THREAD_NAME)).findFirst().orElse(null);
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
        double x, y, z, vx, vy, vz, radius, mass;
        double VCONST = 658000000D;
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
                    radius = c < bodyCount * .0025D ? 12D * Math.random() : 2D * Math.random(); // a few large bodies
                    mass = radius * SOLAR_MASS * .000005D;
                    bodyQueue.add(new Body(Body.nextID(), x, y, z, vx, vy, vz, mass, (float) radius));
                }
            }
        }
        return bodyQueue;
    }

    private static class ConfigurablesImpl implements Configurables {
        private final ConcurrentLinkedQueue<Body> bodyQueue;
        private final ResultQueueHolder resultQueueHolder;
        private final ComputationRunner computationRunner;
        private final int idOfSun;

        ConfigurablesImpl(ConcurrentLinkedQueue<Body> bodyQueue, ResultQueueHolder resultQueueHolder,
                          ComputationRunner computationRunner, int idOfSun) {
            this.bodyQueue = bodyQueue;
            this.resultQueueHolder = resultQueueHolder;
            this.computationRunner = computationRunner;
            this.idOfSun = idOfSun;
        }
        @Override
        public void setResultQueueSize(int queueSize)  {
            resultQueueHolder.setMaxQueues(queueSize);
        }

        @Override
        public int getResultQueueSize() {
            return resultQueueHolder.getMaxQueues();
        }

        @Override
        public void setSmoothing(double smoothing)  {
            computationRunner.setTimeScaling(smoothing);
        }

        @Override
        public double getSmoothing() {
            return computationRunner.getTimeScaling();
        }

        @Override
        public void setComputationThreads(int threads)  {
            computationRunner.setPoolSize(threads);
        }

        @Override
        public int getComputationThreads() {
            return computationRunner.getPoolSize();
        }

        @Override
        public void setCollisionBehavior(CollisionBehavior behavior)  {
            // not currently supported - should err?
        }

        @Override
        public CollisionBehavior getCollisionBehavior() {
            return CollisionBehavior.SUBSUME;
        }

        @Override
        public void removeBodies(int bodyCount)  {
            Random r = new Random();
            int modCount = 0;
            while (modCount < bodyCount) {
                int idx = r.nextInt(bodyQueue.size());
                for (Body b : bodyQueue) {
                    if (idx-- <= 0) {
                        if (b.getId() != idOfSun && b.exists()) {
                            b.setNotExists();
                            modCount++;
                        }
                        break;
                    }
                }
            }
            logger.info("Set {} bodies to not exist", modCount);
        }

        @Override
        public int getBodyCount() {
            return bodyQueue.size();
        }

        @Override
        public void addBody(double mass, double x, double y, double z, double vx, double vy, double vz,
                            double radius)  {
            bodyQueue.add(new Body(Body.nextID(), x, y, z, vx, vy, vz, mass, (float) radius));
        }
    }
}
