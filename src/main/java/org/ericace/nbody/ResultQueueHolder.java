package org.ericace.nbody;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A queue of queues.
 * <p>
 * The nested {@link ResultQueue} class holds the result of one computation cycle: the positions of all the bodies
 * in the sim. There can be multiple of these result queues enqueued in the outer class if the computation thread
 * is faster than the render thread. However there is a limit to the number of result queues the class can
 * hold. Once that limit is reached, the computation thread won't do any additional work until the render
 * thread catches draws the queue down to less than its max size.</p>
 * <p>
 * Storing the computation results outside of the bodies used to perform the calculation lets the rendering
 * engine have access to the computation results without any thread synchronization between the compute thread(s)
 * and the rendering thread. Though there is the expense of continually allocating and freeing the storage.</p>
 */
final class ResultQueueHolder {
    private static final Logger logger = LogManager.getLogger(ResultQueueHolder.class);

    /**
     * The maximum number of {@link ResultQueue} instances that the class will hold
     */
    private final int maxQueues;

    /**
     * Allows assignment of a unique increasing ID to each result queue
     */
    private int queNum = 0;

    /**
     * The queue of queues
     */
    private final ConcurrentLinkedDeque<ResultQueue> queues;

    /**
     * Holds the result of one computation cycle: all bodies in the simulation with whatever info is
     * needed by the rendering engine
     */
    static final class ResultQueue {
        /**
         * Initially a queue is created as un-computed. Once it is filled with all the bodies in the
         * sim - the class consumer sets the queue to computed. The rendering engine won't act on the
         * result queue until it is computed.
         */
        private volatile boolean computed = false;

        /**
         * Diagnostic aid - monotonically increasing value
         */
        private final int queNum;

        /**
         * The bodies to render
         */
        private final List<BodyRenderInfo> queue;

        /**
         * Creates an instance with an initial capacity
         *
         * @param capacity the initial number of BodyRenderInfo instances that will be enqueued (could grow)
         */
        ResultQueue(int queNum, int capacity) {
            queue = new ArrayList<>(capacity);
            this.queNum = queNum;
        }

        /**
         * Sets the queue to computed. The graphics engine checks this and will not consume the
         * queue until it is computed
         */
        void setComputed() {
            computed = true;
        }

        /**
         * Adds the passed instance to the result queue
         *
         * @param bodyRenderInfo whatever is needed by the graphics engine to render the body
         */
        void addRenderInfo(BodyRenderInfo bodyRenderInfo) {
            if (bodyRenderInfo != null) {
                queue.add(bodyRenderInfo);
            }
        }

        /**
         * @return the queue managed by the class
         */
        List <BodyRenderInfo> getQueue() {
            return queue;
        }

        /**
         * @return see {@link #queNum}
         */
        int getQueNum() {
            return queNum;
        }
    }

    /**
     * Creates an instance with the specified capacity
     *
     * @param maxQueues the maximum number of result queues that are allowed. (See {@link #newQueue})
     */
    ResultQueueHolder(int maxQueues) {
        this.maxQueues = maxQueues;
        queues = new ConcurrentLinkedDeque<>();
    }

    /**
     * Creates a new result queue of the specified capacity
     *
     * @param capacity the number of bodyRenderInfo instances in the result queue
     *
     * @return the created queue, or null if the max number of result queues are already
     * held by the instance
     */
    ResultQueue newQueue(int capacity) {
        if (isFull()) {
            return null;
        }
        ResultQueue rq = new ResultQueue(nextQueNum(), capacity);
        queues.add(rq);
        logger.info("Adding result queue ID {} with size={}", rq.queNum, capacity);
        return rq;
    }

    /**
     * @return true if the queue is at capacity and therefore cannot be increased
     * via a call to {@link #newQueue}
     */
    boolean isFull() {
        return queues.size() >= maxQueues;
    }

    /**
     * @return the next computed queue or null if there are no computed queues. Queues are returned in order
     * so that the positions of the bodies in the scene graph change in the order they were computed
     */
    ResultQueue nextComputedQueue() {
        try {
            return queues.getFirst().computed ? queues.poll() : null;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * @return next queue number, wrapping: it's only a diagnostic aid
     */
    private int nextQueNum() {
        return queNum = (queNum == Integer.MAX_VALUE ? 0 : queNum + 1);
    }
}
