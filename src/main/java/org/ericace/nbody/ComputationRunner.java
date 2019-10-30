package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This class runs the n-body computation perpetually within a thread, until the {@link #stopRunner()} method
 * is called. It stores each compute cycle's result in a {@link ResultQueueHolder}, unless the holder is already
 * full at the time the compute cycle starts, in which case that compute cycle is skipped.
 */
class ComputationRunner implements Runnable {
    private static final Logger logger = LogManager.getLogger(ComputationRunner.class);

    /**
     * Set to false via the {@link #stopRunner()} method to stop the runner
     */
    private volatile boolean running = true;

    /**
     * Runs computation workers that compute the force on each body in the sim
     */
    private final ThreadPoolExecutor executor;

    /**
     * The queue of bodies representing the simulation
     */
    private final ConcurrentLinkedQueue<Body> bodyQueue;

    /**
     * A fudge factor that smooths and slows the simulation
     */
    private final double timeScaling;

    /**
     * Holds the  results of each computation cycle. The result of each computation cycle is a queue
     * containing one {@link BodyRenderInfo} instance for each body in the simulation. The renderer class
     * pulls one queue each time its rendering method is invoked.
     */
    private final ResultQueueHolder resultQueueHolder;

    /**
     * Constructor
     *
     * @param threadCount       Number of threads in the executor
     * @param bodyQueue         Bodies in the simulation
     * @param timeScaling       To slow down and smooth out the body movement
     * @param resultQueueHolder Where the compute results are placed
     */
    ComputationRunner(int threadCount, ConcurrentLinkedQueue<Body> bodyQueue, double timeScaling,
                      ResultQueueHolder resultQueueHolder) {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
        this.bodyQueue = bodyQueue;
        this.timeScaling = timeScaling;
        this.resultQueueHolder = resultQueueHolder;
    }

    /**
     * Stops the runner
     */
    void stopRunner() {
        running = false;
    }

    /**
     * Enables the thread pool size to be changed to tinker with throughput
     *
     * @param threadCount the new thread count
     */
    void setPoolSize(int threadCount) {
        executor.setMaximumPoolSize(threadCount);
    }

    /**
     * Runs the n-body force calculation continually
     */
    @Override
    public void run() {
        while (running) {
            try {
                runOneComputation();
            } catch (InterruptedException e) {
                running = false;
            } catch (Exception e) {
                logger.error("WorkRunner exception", e);
            }
        }
    }

    /**
     * Runs one computation. Executes a nested loop:
     * <pre>
     *   for each body
     *     for each other body
     *       compute the force on body from other body
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
     * @throws InterruptedException if interrupted waiting for the countdown latch
     */
    private void runOneComputation() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ResultQueueHolder.ResultQueue rq = resultQueueHolder.newQueue(bodyQueue.size());
        if (rq == null) {
            logger.warn("No more queues");
            return;
        }
        CountDownLatch latch = new CountDownLatch(bodyQueue.size());
        for (Body body : bodyQueue) {
            executor.execute(body.new ForceComputer(bodyQueue, latch));
        }
        // wait for all work to complete
        latch.await();
        for (Body body : bodyQueue) {
            rq.addRenderInfo(body.update(timeScaling));
            if (!body.exists()) {
                // The body was subsumed into another and will still be placed into the result queue so
                // the graphics engine can remove it from the scene graph
                bodyQueue.remove(body);
            }
        }
        rq.setComputed();
        logger.info("Computation finished in {} ms", System.currentTimeMillis() - startTime);
    }
}
