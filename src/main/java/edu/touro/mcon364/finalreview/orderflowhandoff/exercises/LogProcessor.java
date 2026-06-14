package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.LogLevel;
import edu.touro.mcon364.finalreview.model.LogMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * LogProcessor.
 *
 * A server receives log messages from different parts of an application:
 * authentication, payments, reporting, background jobs, and so on. Messages may
 * arrive while earlier messages are still being processed. We want one part of
 * the program to submit log messages, and a small group of worker threads to
 * process those messages in the background.
 *
 * This class represents that log-processing service.
 *
 * The main problem you are solving:
 * - incoming messages need to wait somewhere until a worker is ready for them;
 * - more than one worker may be running at the same time;
 * - every submitted message should be processed once;
 * - while messages are processed, the class must keep accurate summary counts.
 *
 * Requirements:
 * - submit(message) accepts one log message for later processing.
 * - start(workerCount) starts exactly workerCount background workers.
 * - workerCount must be positive.
 * - workers should keep processing while the processor is still accepting work
 *   or while there is still unprocessed work waiting.
 * - stop() tells the processor to stop accepting/expecting more work and waits
 *   until the already-submitted work has been handled.
 * - getTotalProcessed() returns how many log messages have been processed.
 * - getCountsByLevel() returns how many processed messages there were for each
 *   LogLevel.
 * - getCountsByLevel() must not allow callers to mutate this class's internal
 *   state.
 * - The class must behave correctly when multiple threads interact with it.
 *
 * Questions to think about before coding:
 * - Where should submitted messages wait before a worker processes them?
 *   in a queue?
 *
 * - What behavior do we need from that structure: newest first, oldest first,
 *   priority order, or something else?
 *   oldest first, fifo
 *
 * - Which state is shared by multiple threads?
 *
 *
 * - Which operations must be protected so the statistics stay correct?
 *
 *
 * - How will worker threads know when to continue waiting for work and when to
 *   finish?
 *   stop() method
 *
 * - What should happen if stop() is called while messages are still waiting?
 * wait to handle those messages but stop accepting new ones
 *
 * - What should the public getter methods return so outside code cannot damage
 *   the processor's internal state?
 */
public class LogProcessor {
    // queue is shared so reg Arraydeque is not enough, BlockingQueue is thread safe
    private final BlockingQueue<LogMessage> queue = new LinkedBlockingQueue<>();

    private final List<Thread> workers = new ArrayList<>();

    // Atomic Integer lets multiple threads increment safely without a lock
    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    private final ConcurrentHashMap<LogLevel, AtomicInteger> processed = new ConcurrentHashMap<>();

    // volatile to ensure all threads see the latest value of running
    private volatile boolean running = false;
    /*
     * Decide what fields this class needs.
     *
     * Think about:
     * - pending work
     * - worker threads
     * - whether the processor is still running
     * - total processed count
     * - count by log level
     */

    /**
     * Accept one message for processing.
     */
    public void submit(LogMessage message) {
        // TODO: add the message to the queue
        if (running) {
            queue.offer(message);
        }
    }

    /**
     * Start the requested number of background workers.
     */
    public void start(int workerCount) {
        // TODO: implement  start(workerCount) starts exactly workerCount background workers.
        // * - workerCount must be positive.
        // * - workers should keep processing while the processor is still accepting work
        // *   or while there is still unprocessed work waiting.
        // validation
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount must be greater than 0");
        }
        // set running flag to true
        running = true;
        IntStream.range(0, workerCount).forEach(i -> {
            Thread worker = new Thread(this::workerLoop);
            worker.start();
            workers.add(worker);
        });
    }

    /**
     * The work done by one background worker.
     *
     * You may keep this helper method, rename it, or replace it with another
     * private helper if your design is clearer that way.
     */
    private void workerLoop() {
        // keep looping while either accepting new work or unprocessed work is waiting
        while (running || !queue.isEmpty()) {
            try {
                LogMessage message = queue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    process(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

    }
    }

    /**
     * Process one message and update whatever statistics this class tracks.
     */
    private void process(LogMessage message) {
        // computeIfAbsent get existing AtomicInteger for this level, or create one if missing
        processed.computeIfAbsent(message.level(), k -> new AtomicInteger(0)).incrementAndGet();
        totalProcessed.incrementAndGet();


    }

    /**
     * Stop the processor and wait for worker threads to finish.
     */
    public void stop() throws InterruptedException {
        //  mark running as false, and then join all worker threads
        running = false; // workers should stop accepting new work
        workers.forEach(w -> {
            try {
                w.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

    }

    /**
     * Return the number of messages processed so far.
     */
    public int getTotalProcessed() {
        // return atomic count
        return totalProcessed.get();
    }

    /**
     * Return a safe snapshot of the counts by level.
     */
    public Map<LogLevel, Integer> getCountsByLevel() {
        // TODO: defensive copy as Map.CopyOf()
        return Map.copyOf(processed.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get() // convert AtomicInteger to plain Integer
                ))
                );
    }
}
