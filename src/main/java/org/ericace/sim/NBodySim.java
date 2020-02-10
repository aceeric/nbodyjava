package org.ericace.sim;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ericace.grpcserver.NBodyServiceServer;
import org.ericace.instrumentation.Instrumentation;
import org.ericace.instrumentation.InstrumentationManager;
import org.ericace.nbody.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simulation runner
 */
class NBodySim {
    private static final Logger logger = LogManager.getLogger(NBodySim.class);
    private static final Instrumentation instrumentation = InstrumentationManager.getInstrumentation();

    private static final int DEFAULT_MAX_RESULT_QUEUES = 10;
    private static final String JME_THREAD_NAME = "jME3 Main";

    /**
     * Simulation runner
     *
     * <ol>
     *     <li>Initializes instrumentation which - depending on JVM properties - could be
     *         NOP instrumentation, or Prometheus instrumentation</li>
     *     <li>Initializes a queue to hold all the bodies in the simulation from the passed {@code bodies} param</li>
     *     <li>Initializes a result queue holder to hold computed results</li>
     *     <li>Initializes a computation runner and starts it, which perpetually computes the body forces in a thread,
     *         placing the computed results into the result queue holder</li>
     *     <li>Initializes a JMonkey App and starts it which renders the computed results from the result queue
     *         perpetually in a thread</li>
     *     <li>Starts a gRPC server to handle requests from external entities to modify various
     *         aspects of the simulation</li>
     *     <li>Waits for the JMonkey engine thread to exit</li>
     *     <li>Cleans up on exit</li>
     * </ol>
     *
     * @param bodies     A list of bodies to run the simulation with
     * @param threads    The number of threads to use for the computation runner
     * @param scaling    The time scaling factor, which speeds or slows the sim
     * @param initialCam The initial camera position
     * @param simThread  If not null, then the method will call the {@code start} method on the instance after
     *                   the sim is started. The {@code start} method is expected to start a thread which will
     *                   then modify the body queue while the sim is running.
     */
    void run(List<Body> bodies, int threads, double scaling, SimpleVector initialCam, SimThread simThread) {
        try {
            ConcurrentLinkedQueue<Body> bodyQueue = new ConcurrentLinkedQueue<>(bodies);
            ResultQueueHolder resultQueueHolder = new ResultQueueHolder(DEFAULT_MAX_RESULT_QUEUES);
            JMEApp.start(bodies.size(), resultQueueHolder, initialCam);
            ComputationRunner.start(threads, bodyQueue, scaling, resultQueueHolder);
            NBodyServiceServer.start(new ConfigurablesImpl(bodyQueue, resultQueueHolder, ComputationRunner.getInstance()));
            if (simThread != null) {
                simThread.start(bodyQueue);
            }
            getJmeThread().join();
        } catch (Exception e) {
            logger.error("Simulation error", e);
        } finally {
            if (simThread != null) {
                simThread.stop();
            }
            NBodyServiceServer.stop();
            ComputationRunner.stop();
            instrumentation.stop();
        }
        logger.info("Exiting the simulation");
    }

    /**
     * Runs a sim without a callback. See {@link #run(List, int, double, SimpleVector, SimThread)} for details
     */
    void run(List<Body> bodies, int threads, double scaling, SimpleVector initialCam) {
        run(bodies, threads, scaling, initialCam, null);
    }

    /**
     * @return the JME thread, or throw a RuntimeException
     */
    private static Thread getJmeThread() {
        Thread thread = Thread.getAllStackTraces().keySet()
                .stream()
                .filter(t -> t.getName().equals(JME_THREAD_NAME)).findFirst().orElse(null);
        if (thread == null) {
            throw new RuntimeException("Unable to find the JME thread");
        }
        return thread;
    }

    /**
     * Handles the requests from the gRPC server to get and set configurables affecting the
     * behavior of the simulation. This is a facade class that delegates everything to the
     * {@link #resultQueueHolder}, {@link #bodyQueue}, and {@link #computationRunner} instance
     * fields. The class is called by the gRPC server.
     *
     * @see NBodyServiceServer
     */
    private static class ConfigurablesImpl implements Configurables {
        /**
         * Holds all the bodies in the simulation
         */
        private final ConcurrentLinkedQueue<Body> bodyQueue;

        /**
         * Holds computation results that are provided to the rendering engine
         */
        private final ResultQueueHolder resultQueueHolder;

        /**
         * Has a thread that continually computes force and position of bodies in the simulation
         */
        private final ComputationRunner computationRunner;

        /**
         * Saves the passed refs to instance fields with the same name to delegate calls to
         */
        ConfigurablesImpl(ConcurrentLinkedQueue<Body> bodyQueue, ResultQueueHolder resultQueueHolder,
                          ComputationRunner computationRunner) {
            this.bodyQueue = bodyQueue;
            this.resultQueueHolder = resultQueueHolder;
            this.computationRunner = computationRunner;
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
        public void setRestitutionCoefficient(double R) {
            Body.setRestitutionCoefficient(R);
        }

        @Override
        public double getRestitutionCoefficient() {
            return Body.getRestitutionCoefficient();
        }

        /**
         * Makes a best effort to remove the passed number of bodies from the simulation, with the removals
         * distributed evenly across the body queue. Suns aren't removed. Since the queue can be changing con
         * concurrently it might not be possible to remove exactly the specified number of bodies.
         *
         * @param countToRemove the number of bodies to remove
         */
        @Override
        public void removeBodies(int countToRemove)  {
            int removedCnt = 0;
            int step = countToRemove < 0 || countToRemove > bodyQueue.size() ? 1 : bodyQueue.size() / countToRemove;
            int iter = 0;
            boolean shouldRemove = false;
            for (Body b : bodyQueue) {
                if (iter++ % step == 0) {
                    shouldRemove = true;
                }
                if (shouldRemove && !b.isSun() && b.exists()) {
                    b.setNotExists();
                    shouldRemove = false;
                    if (++removedCnt >= countToRemove) {
                        break;
                    }
                }
            }
            logger.info("Set {} bodies to not exist", removedCnt);
        }

        @Override
        public int getBodyCount() {
            return bodyQueue.size();
        }

        @Override
        public void addBody(double mass, double x, double y, double z, double vx, double vy, double vz,
                            double radius, boolean isSun, Body.CollisionBehavior behavior, Body.Color bodyColor,
                            double fragFactor)  {
            Body b = new Body(Body.nextID(), x, y, z, vx, vy, vz, mass, (float) radius, behavior, bodyColor, fragFactor);
            if (isSun) {
                b.setSun();
            }
            bodyQueue.add(b);
        }
    }
}
