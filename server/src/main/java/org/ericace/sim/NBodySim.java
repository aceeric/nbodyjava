package org.ericace.sim;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ericace.globals.Globals;
import org.ericace.grpcserver.NBodyServiceServer;
import org.ericace.instrumentation.Instrumentation;
import org.ericace.instrumentation.InstrumentationManager;
import org.ericace.nbody.*;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Simulation runner
 */
class NBodySim {
    private static final Logger logger = LogManager.getLogger(NBodySim.class);
    private static final Instrumentation instrumentation = InstrumentationManager.getInstrumentation();

    /**
     * Default value for the number of result queues
     */
    private static final int DEFAULT_MAX_RESULT_QUEUES = 10;

    /**
     * JMonkeyEngine thread name
     */
    private static final String JME_THREAD_NAME = "jME3 Main";

    /**
     * If no rendering, then the amount of time to sleep between polling the result queue. Goal is the
     * ensure the computation runner is running at full throttle
     */
    private static final int NO_RENDER_SLEEP_MS = 5;

    /**
     * A list of bodies to start the simulation with
     */
    private List<Body> bodies;

    /**
     * The number of threads to use for the computation runner
     */
    private int threads;

    /**
     * The time scaling factor, which speeds or slows the sim
     */
    private float scaling;

    /**
     * The initial camera position
     */
    private SimpleVector initialCam;

    /**
     * If not null, then the method will call the {@code start} method on the instance after
     * the sim is started. The {@code start} method is expected to start a thread which will
     * then modify the body queue while the sim is running.
     */
    private SimThread simThread;

    /**
     * If false, then don't start the rendering engine. Useful for testing/debugging since the
     * rendering engine and OpenGL can interfere with single-stepping in the IDE
     */
    private boolean render;

    /**
     * Screen resolution. Note - depending on the resolution specified, on a dual monitor system the OpenGL
     * subsystem may locate the sim window on a monitor of its choosing, rather than on the primary monitor
     */
    private int [] resolution;

    /**
     * If true, configure the rendering engine to sync to the monitor vsync. Overrides frame rate.
     */
    private boolean vSync;

    /**
     * Frame rate. If -1, don't set the frame rate (leave it as defaulted by the rendering engine)
     */
    private int frameRate;

    /**
     * Simulation runner
     *
     * <ol>
     *     <li>Initializes instrumentation which - depending on JVM properties - could be
     *         NOP instrumentation, or Prometheus instrumentation</li>
     *     <li>Initializes a queue to hold all the bodies in the simulation from the {@link #bodies} field</li>
     *     <li>Initializes a result queue holder to hold computed results</li>
     *     <li>Initializes a computation runner and starts it, which perpetually computes the sim in a thread,
     *         placing the computed results into the result queue holder</li>
     *     <li>Initializes a JMonkey App and starts it - which renders the computed results from the result queue
     *         perpetually in a thread</li>
     *     <li>Starts a gRPC server to handle requests from external entities to modify various
     *         aspects of the simulation</li>
     *     <li>Waits for the JMonkey engine thread to exit</li>
     *     <li>Cleans up</li>
     * </ol>
     */
    void run() {
        try {
            ConcurrentLinkedQueue<Body> bodyQueue = new ConcurrentLinkedQueue<>(bodies);
            ResultQueueHolder resultQueueHolder = new ResultQueueHolder(DEFAULT_MAX_RESULT_QUEUES);
            if (render) {
                JMEApp.start(bodies.size(), resultQueueHolder, initialCam, resolution, vSync, frameRate, JME_THREAD_NAME);
            }
            ComputationRunner.start(threads, bodyQueue, scaling, resultQueueHolder);
            NBodyServiceServer.start(new ConfigurablesImpl(bodyQueue, resultQueueHolder, ComputationRunner.getInstance()));
            if (simThread != null) {
                simThread.start(bodyQueue);
            }
            getJmeThread(render, resultQueueHolder).join();
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
     * If rendering, return the JME thread or throw a RuntimeException. If not rendering, start a thread
     * and return it so the caller's logic is identical in both cases. If starting a thread, then the started
     * thread will consume the passed {@code resultQueueHolder} so the {@link ComputationRunner} can run at
     * max throughput. This is useful for testing the max number of bodies that the Computation Runner can compute
     * and still stay within the target 50-60 frames (computation cycles) per second. It factors the rendering
     * engine's performance out.
     *
     * @param render            True if rendering, else false: not rendering
     * @param resultQueueHolder If not rendering, the created thread will consume this queue
     *
     * @return the JME thread or the created thread, based on the {@code render} arg
     */
    private static Thread getJmeThread(boolean render, ResultQueueHolder resultQueueHolder) {
        Thread thread;
        if (render) {
            thread = Thread.getAllStackTraces().keySet()
                    .stream()
                    .filter(t -> t.getName().equals(JME_THREAD_NAME)).findFirst().orElse(null);
            if (thread == null) {
                throw new RuntimeException("Unable to find the JME thread");
            }
        } else {
            thread = new Thread(() -> {
                boolean running = true;
                while (running) {
                    try {
                        if (resultQueueHolder.nextComputedQueue() == null) {
                            Thread.sleep(NO_RENDER_SLEEP_MS);
                        }
                    } catch (InterruptedException e) {
                        running = false;
                    }
                }
            });
            thread.start();
        }
        return thread;
    }

    /**
     * Handles the requests from the gRPC server to get and set configurables affecting the
     * behavior of the simulation. This is a facade class that delegates everything to the
     * {@link #resultQueueHolder}, {@link #bodyQueue}, and {@link #computationRunner} instance
     * fields. The class is called by the gRPC server: {@link NBodyServiceServer}
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
        public void setSmoothing(float smoothing)  {
            computationRunner.setTimeScaling(smoothing);
        }

        @Override
        public float getSmoothing() {
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
        public void setRestitutionCoefficient(float R) {
            Body.setRestitutionCoefficient(R);
        }

        @Override
        public float getRestitutionCoefficient() {
            return Body.getRestitutionCoefficient();
        }

        /**
         * Makes a best effort to remove the passed number of bodies from the simulation, with the removals
         * distributed evenly across the body queue. Pinned objects aren't removed (with the exception described
         * in the {@code countToRemove} arg below. Since the queue can be changing concurrently it might
         * not be possible to remove exactly the specified number of bodies.
         *
         * @param countToRemove the number of bodies to remove. If -1, remove everything, even pinned
         *                      bodies
         */
        @Override
        public void removeBodies(int countToRemove)  {
            if (countToRemove == -1) {
                for (Body b : bodyQueue) {
                    b.setNotExists();
                }
                return;
            }
            int removedCnt = 0;
            int step = countToRemove > bodyQueue.size() ? 1 : bodyQueue.size() / countToRemove;
            int iter = 0;
            boolean shouldRemove = false;
            for (Body b : bodyQueue) {
                if (iter++ % step == 0) {
                    shouldRemove = true;
                }
                if (shouldRemove && !b.isPinned() && b.exists()) {
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
        public int addBody(float mass, float x, float y, float z, float vx, float vy, float vz,
                           float radius, boolean isSun, Globals.CollisionBehavior behavior, Globals.Color bodyColor,
                           float fragFactor, float fragStep, boolean withTelemetry, String name, String clas,
                           boolean pinned)  {
            Body b = new Body(Body.nextID(), x, y, z, vx, vy, vz, mass, radius, behavior, bodyColor, fragFactor,
                    fragStep, withTelemetry, name, clas, pinned);
            if (isSun) {
                b.setSun();
            }
            bodyQueue.add(b);
            return b.getId();
        }

        @Override
        public ModBodyResult modBody(int id, String bodyName, String bodyClass, List<BodyMod> bodyMods)  {
            int modified = 0;
            int found = 0;
            for (Body b : bodyQueue) {
                if (!StringUtils.isEmpty(bodyClass) && bodyClass.equalsIgnoreCase(b.getClas()) ||
                    !StringUtils.isEmpty(bodyName) && bodyName.equalsIgnoreCase(b.getName()) ||
                    id == b.getId()) {
                    ++found;
                    modified += b.mod(bodyMods) ? 1 : 0;
                }
            }
            if (found == 0) return ModBodyResult.NO_MATCH;
            if (modified == 0) return ModBodyResult.MOD_NONE;
            if (found == modified) return ModBodyResult.MOD_ALL;
            return ModBodyResult.MOD_SOME;
        }

        @Override
        public Body getBody(int id, String bodyName) {
            for (Body b : bodyQueue) {
                if (!StringUtils.isEmpty(bodyName) && bodyName.equalsIgnoreCase(b.getName()) || id == b.getId()) {
                    return b;
                }
            }
            return null;
        }
    }

    /**
     * Constructor from builder
     */
    private NBodySim(Builder builder) {
        this.bodies = builder.bodies;
        this.threads = builder.threads;
        this.scaling = builder.scaling;
        this.initialCam = builder.initialCam;
        this.simThread = builder.simThread;
        this.render = builder.render;
        this.resolution = builder.resolution;
        this.vSync = builder.vSync;
        this.frameRate = builder.frameRate;
    }

    /**
     * Standard builder pattern
     */
    static class Builder {
        private List<Body> bodies;
        private int threads;
        private float scaling;
        private SimpleVector initialCam;
        private SimThread simThread;
        private boolean render;
        private int [] resolution;
        private boolean vSync;
        private int frameRate;

        Builder bodies(List<Body> bodies) {
            this.bodies = bodies;
            return this;
        }
        Builder threads(int threads) {
            this.threads = threads;
            return this;
        }
        Builder scaling(float scaling) {
            this.scaling = scaling;
            return this;
        }
        Builder initialCam(SimpleVector initialCam) {
            this.initialCam = initialCam;
            return this;
        }
        Builder simThread(SimThread simThread) {
            this.simThread = simThread;
            return this;
        }
        Builder render(boolean render) {
            this.render = render;
            return this;
        }
        Builder resolution(int [] resolution) {
            this.resolution = resolution;
            return this;
        }
        Builder vSync(boolean vSync) {
            this.vSync = vSync;
            return this;
        }
        Builder frameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }
        NBodySim build() {
            return new NBodySim(this);
        }
    }
}
