package edu.touro.mcon364.finalreview.orderflowhandoff.homework;

import edu.touro.mcon364.finalreview.model.SensorReading;

import java.util.DoubleSummaryStatistics;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Homework 2 — Sensor reading processor.
 *
 * A monitoring system receives readings from sensors over time. One part of the
 * program submits readings as they arrive. Another part of the program processes
 * those readings using one or more background workers.
 * --> multiple threads will be submitting and processing readings at the same time.
 *     this is a thread-safety problem, and we need to coordinate the handoff of work between those threads.
 * This class is responsible for coordinating that handoff and for keeping a
 * summary of the readings that were actually processed.
 * --> we need to store the submitted readings somewhere until they are processed,
 *     and we need to keep track of the summary statistics for the processed readings.
 * The important question is not only "How do we calculate the stats?" It is also:
 * "What happens when readings are being submitted and processed by different
 * threads at the same time?"
 *
 * Requirements:
 * - submit(reading) accepts one new sensor reading for later processing.
 *   --> you should put the reading on a thread-safe queue or collection for later processing by the workers.
 * - start(workerCount) starts workerCount background workers.
 *   --> You should create a thread pool and submit workerCount tasks to it,
 * - workerCount must be greater than 0.
 *   --> You should validate the input and throw an exception if it's not.
 * - Workers should process submitted readings until the processor is stopped and
 *   all already-submitted readings have been handled.
 *   --> Your worker runnable should check both the running state and the queue state to decide when to exit.
 * - stop() tells the processor to stop accepting/processing future work and waits
 *   until the workers finish the remaining work.
 *   --> You should set a flag to stop accepting new work, interrupt the worker threads to wake them up if they're waiting,
 *       and then wait for them to finish.
 * - getTotalProcessed() returns how many readings have been processed so far.
 *   --> You should use an AtomicInteger to keep track of the total count of processed readings, and return its value here.
 * - getStats() returns summary statistics for the processed reading values:
 *   count, minimum, maximum, sum, and average.
 *   --> You should use a DoubleSummaryStatistics object to keep track of the summary statistics, and return a snapshot of it here.
 * - Public reporting methods must not expose mutable internal state.
 *
 *
 * Before coding, think about:
 * - Which object or objects represent work waiting to be processed?
 *   --> A thread-safe queue or collection (e.g., a BlockingQueue) can represent the work waiting to be processed.
 * - Which object or objects represent work that has already been processed?
 *   --> An AtomicInteger can represent the count of processed readings, and a DoubleSummaryStatistics object can represent the summary statistics for the processed readings.
 * - Which state can be accessed by more than one thread?
 *   --> The queue of submitted readings, the count of processed readings, and the summary statistics can all be accessed by multiple threads, so they need to be thread-safe.
 * - How will workers know when to keep working and when to stop?
 *   --> The workers can check a running flag to know when to stop accepting new work, and they can also check the queue to see if there is still work to be done before exiting.
 * - What should happen if getStats() is called while workers are still running?
 *   --> getStats() should return a snapshot of the current statistics, which may be changing as workers process readings.
 *       You should ensure that the returned statistics are consistent and not affected by concurrent updates.
 * - Is it better to store all processed readings and calculate stats later, or
 *   update numeric summary state as each reading is processed?
 *   --> It is more efficient to update the summary statistics as each reading is processed, rather than storing all readings and calculating stats later.
 *   This way, you can keep track of the count, minimum, maximum, sum, and average in a single pass without needing to store all individual readings.
 * - If several workers update the same stats, how will those updates stay correct?
 *  --> You should ensure that updates to the summary statistics are thread-safe.
 *  --> alternatively you can keep readings in a thread-safe collection and calculate stats in getStats(), but that would be less efficient and more memory-intensive.
 */
public class SensorProcessor {

    private final BlockingQueue<SensorReading> queue = new LinkedBlockingQueue<>();
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
    private final AtomicReference<DoubleSummaryStatistics> stats = new AtomicReference<>(new DoubleSummaryStatistics());

    private  ExecutorService executor;

    private volatile boolean running = true;

    /**
     * Accept one sensor reading for processing.
     *
     * @param reading the reading to process later
     */
    public void submit(SensorReading reading) {
        if(running) {
            queue.offer(reading);
        }
    }

    /**
     * Start background workers that process submitted readings.
     *
     * @param workerCount number of worker threads to start
     * @throws IllegalArgumentException if workerCount is not positive
     */
    public void start(int workerCount) {
        //  validate workerCount
        if (workerCount <= 0)
            throw new IllegalArgumentException("workerCount must be positive");
        //start the requested number of workers
        executor = Executors.newFixedThreadPool(workerCount);
        for (int i = 0; i < workerCount; i++) {
            executor.submit(this::workerLoop);
        }
    }
    private void process(SensorReading reading) {

        totalProcessed.incrementAndGet();
        stats.updateAndGet(existing -> {
            DoubleSummaryStatistics updated = new DoubleSummaryStatistics();

            updated.combine(existing); // combining updated with existing !!

            updated.accept(reading.value());
            return updated;
        });
    }
    /**
     * Logic run by each worker.
     *
     * This method is private because callers should not run worker logic directly.
     * The worker should repeatedly look for work, process it when available, and
     * eventually exit when the processor is stopping and no work remains.
     */
    private void workerLoop() {
        while (running || !queue.isEmpty()) {
            try {
                // Wait for a reading to become available, but time out periodically to check the running state
                SensorReading reading = queue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (reading != null) {
                    process(reading);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop the processor and wait for workers to finish.
     *
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public void stop() throws InterruptedException {
        // switch the running flag to false
        running = false;
        if (executor != null) {
            executor.shutdown();

            // wait for all workers to finish
            executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        }
        while (!queue.isEmpty()) {
            process(queue.poll());
        }
    }

    /**
     * Return the number of readings processed so far.
     */
    public int getTotalProcessed() {

        return totalProcessed.get();
    }

    /**
     * Return summary statistics for the processed reading values.
     *
     * If no readings have been processed yet, return an empty
     * DoubleSummaryStatistics object.
     */
    public DoubleSummaryStatistics getStats() {

        DoubleSummaryStatistics snapshot = new DoubleSummaryStatistics();
        snapshot.combine(stats.get());
        return snapshot;
    }
}
