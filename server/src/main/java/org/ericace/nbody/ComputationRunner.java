package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ericace.instrumentation.InstrumentationManager;
import org.ericace.instrumentation.Metric;

import java.util.concurrent.*;

/**
 * This class runs the n-body computation perpetually within a thread, until the {@link #stop()} method
 * is called. It stores each compute cycle's result in a {@link ResultQueueHolder}, unless the holder is already
 * full at the time the compute cycle starts, in which case that compute cycle is skipped.
 * <p>
 * Each compute cycle runs the n-body computation using a thread pool. The size of the pool is specified
 * in the constructor. Each body in the simulation is scheduled into the thread pool. Once all bodies are
 * scheduled, the computation thread waits for all threads in the pool to complete, and then adds the result
 * to a result queue. The result queue is used by the rendering thread to render the result of the computation.</p>
 *
 * @see ComputationRunner#ComputationRunner(int, ConcurrentLinkedQueue, float, ResultQueueHolder) Constructor
 */
public final class ComputationRunner implements Runnable {
    private static final Logger logger = LogManager.getLogger(ComputationRunner.class);
    private static ComputationRunner instance;

    private static final Metric metricComputationCount = InstrumentationManager.getInstrumentation()
            .registerLabeledCounter("nbody_computation_count/thread", "runner", "Simulation cycles");
    private static final Metric metricComputationThreadsGauge = InstrumentationManager.getInstrumentation()
            .registerGauge("nbody_computation_thread_gauge", "Computation Runner thread pool size");
    private static final Metric metricNoQueuesCount = InstrumentationManager.getInstrumentation()
            .registerCounter("nbody_no_computation_queues_count",
                    "Count of computation runner outrunning rendering engine");
    private static final Metric metricBodyCountGauge = InstrumentationManager.getInstrumentation()
            .registerLabeledGauge("nbody_body_count_gauge/thread", "runner", "Number of bodies in the simulation");

    /**
     * Set to false via the {@link #stop()} method to stop the runner
     */
    private volatile boolean running = true;

    /**
     * Runs computation workers that compute the force on each body in the sim
     */
    private final ThreadPoolExecutor executor;

    /**
     * Wraps the executor
     */
    private final CompletionService<Void> completionService;

    /**
     * The queue of bodies representing the simulation
     */
    private final ConcurrentLinkedQueue<Body> bodyQueue;

    /**
     * Defines the time unit
     */
    private float timeScaling;

    /**
     * Holds the  results of each computation cycle. The result of each computation cycle is a queue
     * containing one {@link BodyRenderInfo} instance for each body in the simulation. The renderer class
     * pulls one queue each time its rendering method is invoked.
     */
    private final ResultQueueHolder resultQueueHolder;

    /**
     * Creates an instance using the specified params - the instance is expected to run in a thread
     *
     * @param threadCount       Number of threads in the executor thread pool: During each computation cycle, each
     *                          body in the simulation is scheduled into the thread pool for force computation
     * @param bodyQueue         Bodies in the simulation
     * @param timeScaling       A factor to slow down and smooth out the simulation movement
     * @param resultQueueHolder Where the compute results are placed
     *
     * @see #run
     */
    private ComputationRunner(int threadCount, ConcurrentLinkedQueue<Body> bodyQueue, float timeScaling,
                              ResultQueueHolder resultQueueHolder) {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
        completionService = new ExecutorCompletionService<>(executor);
        this.bodyQueue = bodyQueue;
        this.timeScaling = timeScaling;
        this.resultQueueHolder = resultQueueHolder;
        metricComputationThreadsGauge.setValue(threadCount);
    }

    /**
     * Creates a new runner, and starts the runner in a thread
     *
     * @param threadCount       Refer to {@link #ComputationRunner}
     * @param bodyQueue         "
     * @param timeScaling       "
     * @param resultQueueHolder "
     */
    public static void start(int threadCount, ConcurrentLinkedQueue<Body> bodyQueue, float timeScaling,
                             ResultQueueHolder resultQueueHolder) {
        instance = new ComputationRunner(threadCount, bodyQueue, timeScaling, resultQueueHolder);
        new Thread(instance).start();
    }
    /**
     * Stops the runner
     */
    public static void stop() {
        if (instance != null) {
            instance.running = false;
        }
    }

    /**
     * @return the singleton
     */
    public static ComputationRunner getInstance() {
        return instance;
    }

    /**
     * @return the thread pool executor max pool size
     */
    public int getPoolSize() {
        return executor.getMaximumPoolSize();
    }

    /**
     * Enables the thread pool size to be changed to tinker with throughput
     *
     * @param threadCount the new thread count
     */
    public void setPoolSize(int threadCount) {
        if (threadCount == executor.getCorePoolSize()) {
            return;
        } else if (threadCount > executor.getCorePoolSize()) {
            executor.setMaximumPoolSize(threadCount);
            executor.setCorePoolSize(threadCount);
        } else {
            executor.setCorePoolSize(threadCount);
            executor.setMaximumPoolSize(threadCount);
        }
        metricComputationThreadsGauge.setValue(threadCount);
    }

    /**
     * @return the current time scaling factor
     */
    public float getTimeScaling() {
        return timeScaling;
    }

    /**
     * sets the new time scaling factor
     * @param timeScaling the value to set
     */
    public void setTimeScaling(float timeScaling) {
        this.timeScaling = timeScaling;
    }

    /**
     * Runs the n-body force calculation continually in a thread
     *
     * @see #runOneComputation
     */
    @Override
    public void run() {
        while (running) {
            try {
                runOneComputation();
            } catch (InterruptedException e) {
                logger.info("ComputationRunner interrupted");
                running = false;
            } catch (Exception e) {
                logger.error("ComputationRunner exception - computation runner is stopping", e);
                running = false;
            }
        }
        logger.info("ComputationRunner stopped");
        executor.shutdownNow();
    }

    /**
     * Runs one computation. Executes a nested loop:
     * <pre>
     *   for each body in bodies
     *     for each other-body in bodies
     *       compute the force on body from other-body
     * </pre>
     * Each body from the outer loop is scheduled into the thread pool, and passed the whole body
     * queue. Therefore, each body is free to update its own force without thread synchronization on the
     * force member fields because its the only body doing that calculation on itself. The application
     * of the total final force to the body velocity and position is deferred until the entire queue
     * of bodies have had their force computed.
     * <p>
     * So at that time, it is safe to update the velocity and position without synchronization because no
     * other threads are reading the bodies. The results are stored in a queue of {@link BodyRenderInfo}
     * instances which the graphics engine consumes. The graphics engine continually gets a copy of the
     * body values (and only what it needs to render the visuals) so there is never thread contention
     * between the graphics engine and the body position computation</p>
     *
     * @throws InterruptedException if interrupted waiting for a computation to complete
     */
    @SuppressWarnings("ConstantConditions") // or IntelliJ complains about rq assignment
    private void runOneComputation() throws InterruptedException {
        if (resultQueueHolder.isFull()) {
            // this thread has outrun the rendering engine
            logger.debug("No more queues");
            metricNoQueuesCount.incValue();
            Thread.sleep(5);
            return;
        }
        int bodyCount = 0;
        for (Body body : bodyQueue) {
            completionService.submit(body.new ForceComputer(bodyQueue));
            ++bodyCount;
        }
        if (bodyCount == 0) {
            // sim is empty at this time, don't peg the CPU
            Thread.sleep(5);
            return;
        }
        metricBodyCountGauge.setValue(bodyCount);
        // blocks until all calculations are complete
        for (int i = 0; i < bodyCount; ++i) {
            completionService.take();
        }
        ResultQueueHolder.ResultQueue rq = resultQueueHolder.newQueue(bodyCount);
        int countRemoved = 0;
        for (Body body : bodyQueue) {
            BodyRenderInfo renderInfo = body.update(timeScaling);
            rq.addRenderInfo(renderInfo);
            if (!body.exists()) {
                // The body no longer exists, but will still be placed into the result queue so
                // the graphics engine can remove it from the scene graph
                bodyQueue.remove(body);
                ++countRemoved;
            }
        }
        rq.setComputed();
        if (countRemoved > 0) {
            logger.debug("Removed {} bodies from the queue", countRemoved);
        }
        metricComputationCount.incValue();
    }
}
