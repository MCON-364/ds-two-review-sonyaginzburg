package edu.touro.mcon364.finalreview.treesandthreads.homework;

import edu.touro.mcon364.finalreview.treesandthreads.model.Book;
import edu.touro.mcon364.finalreview.treesandthreads.model.Employee;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * Homework 1 - Library Catalog (TreeMap + TreeSet + Streams)
 * <p>
 * Scenario: a library stores books. Each book has a title, author, and
 * publication year. The catalog must answer several questions in sorted order.
 * <p>
 * Before coding, think about:
 * - Which structure gives us books sorted by title automatically?
 * TreeMap sorts automatically (Hashmap does not)
 * <p>
 * <p>
 * - Should the author-to-books index use a List or a Set inside the map?
 * <p>
 * <p>
 * <p>
 * What happens if the same book appears twice?
 * <p>
 * <p>
 * <p>
 * - What does NavigableMap.headMap give us, and when would we use it?
 * <p>
 * Requirements:
 * - The constructor receives the list of books to index.
 * - buildTitleIndex() returns a TreeMap keyed by title for O(log n) exact lookups.
 * - buildAuthorIndex() returns a TreeMap grouping books by author; books for each
 * author are sorted by title.
 * - getBooksPublishedBefore(year) returns all books published strictly before
 * the given year, sorted by title.
 * - getAuthorsWithMoreThan(n) returns a sorted list of author names who have
 * more than n books in the catalog.
 * - findByTitlePrefix(prefix) returns all books whose title starts with the
 * given string, alphabetically. Use NavigableMap range operations.
 * <p>
 * Do not use explicit loops. Use streams and collectors.
 */
public class LibraryCatalog {

    private final List<Book> books;

    public LibraryCatalog(List<Book> books) {
        // validate that books is not null
        if (books == null) {
            throw new IllegalArgumentException("Books cannot be null");
        }
        //store a defensive copy so outside code cannot mutate this object
        this.books = List.copyOf(books);
    }

    /**
     * Returns a TreeMap keyed by book title for O(log n) exact lookups.
     * If two books share a title, keep only one (your choice which).
     *
     */
    public TreeMap<String, Book> buildTitleIndex() {
        return books.stream().collect(Collectors.toMap( // takes 4 arguments!!
                book -> book.title(), // key - book title
                book -> book, // value is the object itself
                (existing, duplicate) -> existing,  // what to do with duplicate keys
                TreeMap::new));// creating a new treemap (without this its just a hashmap)

    }

    /**
     * Returns a TreeMap grouping books by author; each author maps to a
     * TreeSet of their books sorted by title.
     */
    public TreeMap<String, TreeSet<Book>> buildAuthorIndex() {
        return books.stream()
                .collect(Collectors.groupingBy(
                        Book::author, // group by author
                        TreeMap::new, // Map supplier, sorted map of authors
                        Collectors.toCollection(TreeSet::new) // this argument in groupingBY is called the downstream collector,
                        // here it is a set of books per author
                ));
    }

    /**
     * Returns all books published strictly before the given year, sorted by title.
     *
     */
    public List<Book> getBooksPublishedBefore(int year) {
        return buildTitleIndex().entrySet().stream() // using buildTitleIndex bc its already sorted
                .filter(entry -> entry.getValue().year() < year) // filter by year, dig into entry to get the book and then the year
                .map(Map.Entry::getValue) // extract the Book from the entry
                .toList();

    }

    /**
     * Returns a sorted list of author names who have more than n books in this catalog.
     *
     */
    public List<String> getAuthorsWithMoreThan(int n) {
        return buildAuthorIndex().entrySet().stream()// using authr index here bc we need books grouped by author
                .filter(entry -> entry.getValue().size() > n)
                .map(Map.Entry::getKey)// extracting just the authors name
                .toList();
    }

    /**
     * Returns all books whose title starts with the given prefix, alphabetically.
     *
     */
    public List<Book> findByTitlePrefix(String prefix) {
        return buildTitleIndex().subMap(prefix, prefix + Character.MAX_VALUE)
                .values()
                .stream() // calling values on the map and then stream on the collection !!
                // no need to map bc streaming values directly !!
                .toList();
    }
}

