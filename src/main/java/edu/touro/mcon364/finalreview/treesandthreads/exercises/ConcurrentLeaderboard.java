package edu.touro.mcon364.finalreview.treesandthreads.exercises;

import edu.touro.mcon364.finalreview.treesandthreads.model.ScoreEntry;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;

/**
 * In-class Exercise 3 - Concurrent Leaderboard (ConcurrentSkipListSet + ExecutorService)
 * <p>
 * Scenario: a game server receives score submissions from many player threads at
 * the same time. The leaderboard must always reflect current top scores in
 * sorted order (highest first) and must be safe when read and written concurrently.
 * <p>
 * This exercise practises:
 * - ConcurrentSkipListSet as the thread-safe sorted cousin of TreeSet.
 * - Why TreeSet is NOT safe for concurrent access.
 * - ExecutorService and Runnable to simulate concurrent score submissions.
 * - AtomicInteger for a safe submission counter.
 * - Stream operations to produce a ranked snapshot from the sorted set.
 * <p>
 * Before coding, think about:
 * - What would happen if two threads called TreeSet.add() simultaneously?
 * thats not safe, for concurrent access
 * <p>
 * - ConcurrentSkipListSet keeps elements sorted by compareTo.
 * Look at ScoreEntry.compareTo: which score appears first in iteration?
 * <p>
 * <p>
 * - Each ScoreEntry is unique by (playerName, score, timestamp).
 * If a player submits a new score, does the old one disappear?
 * no...
 * <p>
 * <p>
 * Requirements:
 * - submitScore(entry) adds a ScoreEntry thread-safely.
 * - getTopN(n) returns the top n ScoreEntry objects as an immutable list, highest first.
 * - getTotalSubmissions() returns the number of times submitScore has been called.
 * - runSimulation(players, scoresEach) uses an ExecutorService to have each player
 * submit scoresEach random scores concurrently, then shuts down the pool and waits.
 * <p>
 * Do not use synchronized blocks. Rely on ConcurrentSkipListSet and AtomicInteger.
 */
public class ConcurrentLeaderboard {

    // ScoreEntry.compareTo sorts highest score first

    // We are using ConcurrentSkipListSet bc it is thread safe and allows for a safe add() from multiple threads
    // (TreeSet is not thread safe!!)
    // Runnable = block of code that runs on a worker thread
    private final ConcurrentSkipListSet<ScoreEntry> leaderboard = new ConcurrentSkipListSet<>();

    // AtomicInteger is a thread safe integer counter
    // (does incrementAndGet() as one operation)
    private final AtomicInteger totalSubmissions = new AtomicInteger(0);

    /**
     * Adds a score entry to the leaderboard thread-safely.
     *
     * @param entry the score entry to add
     */
    public void submitScore(ScoreEntry entry) {
        leaderboard.add(entry); // thread safe bc of ConcurrentSkipListSet
        totalSubmissions.incrementAndGet(); // this is an Atomic Integer so there is no need fir synchronization
    }

    /**
     * Returns the top n scores as an immutable list, highest score first.
     *
     * @param n number of top entries to return
     * @return immutable top-n list
     */
    public List<ScoreEntry> getTopN(int n) {
        //
        return leaderboard.stream() // already in highest order bc of compareTo()
                .limit(n)
                .toList(); // makes an immutable list
    }

    /**
     * Returns how many times submitScore has been called since creation.
     */
    public int getTotalSubmissions() {
        return totalSubmissions.get(); // reads current value of Atomic Integer
    }

    /**
     * Simulates concurrent score submissions using an ExecutorService.
     * <p>
     * Each player in the list submits scoresEach random scores on a separate thread.
     * Wait for all threads to finish before returning.
     *
     * @param players    list of player names
     * @param scoresEach number of random scores each player submits
     */
    // Notes: ExecutorService = managed thread pool
    public void runSimulation(List<String> players, int scoresEach) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());  // availableProcessors is the number of CPU cores on the machine
        // for each score in scoresEach create a scoreEntry with players name and score and then submit it
        players.stream()
                .forEach(player -> pool.submit(() -> {
                    // we use IntStream to repeat the action scoresEach times ( basically a loop)
                    IntStream.range(0, scoresEach)
                        .forEach(i -> submitScore(new ScoreEntry(player, new Random().nextInt(1000), System.currentTimeMillis())));}));
        pool.shutdown(); // means dont accept any new tasks
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }



}
