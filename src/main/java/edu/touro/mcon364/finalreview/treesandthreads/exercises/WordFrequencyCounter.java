package edu.touro.mcon364.finalreview.treesandthreads.exercises;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * In-class Exercise 1 - Word Frequency Counter (TreeMap + Streams)
 *
 * Scenario: you receive a list of words (already lowercased and cleaned).
 * You need to count how many times each word appears and then answer
 * several questions about those counts - all in sorted order.
 *
 * This exercise practises:
 * - Why TreeMap gives us sorted-key iteration for free.
 * - How Collectors.groupingBy + Collectors.counting builds a frequency map.
 * - How NavigableMap operations (firstKey, lastKey, headMap, tailMap) let us
 *   slice the sorted map without iterating manually.
 * - How a stream pipeline can rank or filter the frequency entries.
 *
 * Before coding, think about:
 * - If we use HashMap instead of TreeMap, which methods would break, and why?
 *       TreeMap keeps keys in sorted order and HashMap does not.
 *       TreeMap supports subMap, headMap, tailMAp, firstKey, lastKey
 *
 * - What is the difference between headMap(key) and headMap(key, true)?
 *      headMap(key, True) includes the endpoint key itself
 *
 * - Should getTopN return words with the highest count or the lowest count?
 *      highest which is why we did reversed
 *
 * Requirements:
 * - The constructor receives the list of words to analyze.
 * - buildFrequencyMap() returns a TreeMap<String, Long> where every key is a
 *   unique word and every value is how many times that word appeared.
 * - getTopN(n) returns the n words with the highest frequency, sorted
 *   descending by count. Ties may appear in any order.
 *   Note that you have to sort the frequency map by value, not by key, to get the top N.
 * - getWordsStartingWith(prefix) returns a sorted list of all words whose
 *   first character equals the given prefix character (e.g., 'a').
 * - getMostFrequentInRange(from, to) returns the word with the highest count
 *   among words in the alphabetical range [from, to] inclusive.
 *   Return Optional.empty() if the range is empty.
 *
 * Do not use explicit loops anywhere. Use streams and collectors instead.
 */
public class WordFrequencyCounter {

    private final List<String> words;

    public WordFrequencyCounter(List<String> words) {
        // validate that words is not null
        if (words == null ) {
            throw new IllegalArgumentException("words cannot be null");
        }
        //store a defensive copy so outside code cannot mutate this object
        this.words = List.copyOf(words);
    }

    /**
     * Counts how many times each word appears.
     * The returned map must be sorted alphabetically by word.
     * @return sorted frequency map
     */
    public TreeMap<String, Long> buildFrequencyMap() {
        // Function.identity() means that the word itself is the grouping key
        Map<String, Long> frequencies =
                words.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        // this copies everything into a treemap bc a hashmap doesnt keep keys sorted so this prints it sorted
        return new TreeMap<>(frequencies);
    }

    /**
     * Returns the n most frequent words, highest count first.
     *
     * @param n number of top words to return
     * @return list of words, most frequent first
     */
    public List<String> getTopN(int n) {
        // using already built freq map
        TreeMap<String, Long> freq = buildFrequencyMap();
        // entry set bc u need key and value - gives (word, count) pairs
        return freq.entrySet().stream()
                // sort pairs by coun (getValue) and then reverse so that largest comes first (usually smallest first)
                .sorted(Comparator.comparing(Map.Entry<String, Long>:: getValue).reversed())
                // keep first n entries
                .limit(n)
                // now we need to return the words so map and get the key/word
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Returns all words whose first letter equals the given prefix letter,
     * in alphabetical order.
     *
     * @param prefix the starting letter (e.g., 'b')
     * @return sorted list of matching words
     */
    public List<String> getWordsStartingWith(char prefix) {
        // using buildFrequencyMap bc keys are already unique and its a TreeMap so its already sorted alphabetically
        return buildFrequencyMap().keySet()
                .stream()
                .filter(word -> word.startsWith(String.valueOf(prefix)))
                .toList();
    }

    /**
     * Finds the most frequent word in the alphabetical range [from, to] inclusive.
     *
     *
     * @param from lower bound word (inclusive)
     * @param to   upper bound word (inclusive)
     * @return Optional containing the most frequent word in range, or empty if none
     */
    public Optional<String> getMostFrequentInRange(String from, String to) {
        return buildFrequencyMap()
                // subMap to make the smaller range
                .subMap(from, true, to, true)
                // we need the word and count so we use entrySet
                .entrySet()
                .stream()
                // max returns Optional bc range could be empty
                .max(Comparator.comparing(Map.Entry::getValue))
                // map converts Optional<(word, count)> into Optional <word>
                .map(Map.Entry::getKey);
    }
}
