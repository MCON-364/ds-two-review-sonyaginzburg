package edu.touro.mcon364.finalreview.treesandthreads.homework;

import edu.touro.mcon364.finalreview.treesandthreads.model.Employee;

import java.util.*;
import java.util.stream.*;

/**
 * Homework 2 - Student GradeBook (TreeMap + Streams + DoubleSummaryStatistics)
 *
 * Scenario: a course has many students. Each student is identified by name and
 * has a numeric grade (0.0 to 100.0). The gradebook must support sorted lookup
 * and statistical analysis.
 *
 * Before coding, think about:
 * - Should the map key be the student name or the grade? Why does it matter?
 * shoud be the student name....
 *
 * - What does TreeMap.firstEntry() return? What does lastEntry() return?
 *
 *
 * - How do we turn a numeric score into a letter grade inside a stream?
 *
 * Requirements:
 * - The constructor receives a Map of student name to grade.
 * - buildSortedGradeBook() returns a TreeMap so students are iterated alphabetically.
 * - getStatistics() returns DoubleSummaryStatistics over all grades.
 * - getLetterGradeDistribution() returns a TreeMap counting how many students
 *   received each letter grade: A (90+), B (80-89), C (70-79), D (60-69), F (below 60).
 * - getTopStudents(n) returns the names of the n highest-scoring students, highest first.
 * - getStudentsInScoreRange(low, high) returns a sorted list of student names
 *   whose grade is in [low, high] inclusive.
 *
 * Do not use explicit loops. Use streams and collectors.
 */
public class StudentGradeBook {

    private final Map<String, Double> grades;

    public StudentGradeBook(Map<String, Double> grades) {
        // validate that grades is not null
        if (grades == null) {
            throw new IllegalArgumentException("Grades cannot be null");
        }
        //store a defensive copy so outside code cannot mutate this object
        this.grades = Map.copyOf(grades); // pay attention to the field!!
    }

    /**
     * Returns a TreeMap so iteration visits students alphabetically.
     *
     */
    public TreeMap<String, Double> buildSortedGradeBook() {
        // wrapping it in a treemap so it can be sorted, thats all
        return new TreeMap<>(this.grades);
    }

    /**
     * Returns summary statistics (count, min, max, average, sum) over all grades.
     *
     */
    public DoubleSummaryStatistics getStatistics() {
        return grades.values().stream()// dont forget the values bc u dont need the keys
                .mapToDouble(Double::doubleValue) // eah element is the grades already after grades.values() so just unwrapping Double to double
                .summaryStatistics();
    }

    /**
     * Returns a TreeMap counting students per letter grade.
     *
     */
    public TreeMap<String, Long> getLetterGradeDistribution() {
        // return books.stream()
        //                .collect(Collectors.groupingBy(
        //                        Book::author, // group by author
        //                        TreeMap::new, // Map supplier, sorted map of authors
        //                        Collectors.toCollection(TreeSet::new) // this argument in groupingBY is called the downstream collector,
        //                        // here it is a set of books per author
        return grades.entrySet().stream() // u need entry set
                .collect(Collectors.groupingBy(
                        entry -> {
                            double score = entry.getValue();
                            if (score >= 90) return "A";
                            if (score >= 80) return "B";
                            if (score >= 70) return "C";
                            if  (score >= 60) return "D";
                            return "F";
                        }, // streaming entrySet so each element is a Map.Entry<String, Double>
                        TreeMap::new,
                        Collectors.counting()// we want to count!

                ));
    }

    /**
     * Returns the names of the n highest-scoring students, highest first.
     */
    public List<String> getTopStudents(int n) {
        // return buildTitleIndex().entrySet().stream() // using buildTitleIndex bc its already sorted
          //      .filter(entry -> entry.getValue().year() < year) // filter by year, dig into entry to get the book and then the year
            //    .map(Map.Entry::getValue) // extract the Book from the entry
              //  .toList();
        return buildSortedGradeBook().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .map(Map.Entry::getKey) // extract the name
                .toList();
    }

    /**
     * Returns a sorted list of names whose grade falls in [low, high] inclusive.
     *
     */
    public List<String> getStudentsInScoreRange(double low, double high) {
        // answer why using filter here and not a submap
        // getKey = name, getValue = grade
        return buildSortedGradeBook().entrySet().stream() // using buildSortedGradeBook bc its already sorted
                .filter(entry -> entry.getValue() >= low && entry.getValue() <= high) // filter by grade
                .map(Map.Entry::getKey) // extract the name
                .toList();
    }
}
