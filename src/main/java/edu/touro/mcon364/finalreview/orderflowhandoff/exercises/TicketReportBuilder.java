package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.Priority;
import edu.touro.mcon364.finalreview.model.SupportTicket;
import edu.touro.mcon364.finalreview.model.TicketReport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Building a report from completed work.
 *
 * A support system stores tickets after they have been submitted and worked on.
 * Each ticket has information such as its category, priority, whether it was
 * resolved, and how many minutes it took to resolve.
 *
 * The goal of this class is to turn a list of individual tickets into one
 * summary report. We are not modifying tickets. We are looking across the
 * collection and answering questions about the data.
 * -
 * - The tickets are already available as a collection.
 * - The job is to analyze the collection and produce answers.
 *
 * Before coding, think about the problem in two layers:
 *
 * Layer 1 — What data does this object need?
 * - Should the list of tickets be passed into every method?
 * - Or does it make sense for the builder to receive the list once and then
 *   answer several report questions about that same list?
 * - If this class stores the list, should it keep the original reference or
 *   protect itself with a copy?
 *
 * Layer 2 — What questions does the report ask?
 * - Which questions produce a single number?
 * - Which questions produce a map?
 * - Which questions produce a smaller list?
 * - Which questions require looking only at resolved tickets?
 * - Which questions require looking only at unresolved tickets?
 *
 * Requirements:
 * - The constructor receives the tickets to analyze.
 * - The original list must not be modified by this class.
 * - getResolvedCount() returns how many tickets have been resolved.
 * - getAverageResolutionMinutes() returns the average resolution time for
 *   resolved tickets only.
 * - getCountByCategory() returns how many tickets belong to each category.
 * - getHighPriorityUnresolved() returns unresolved tickets that should receive
 *   the most urgent attention.
 * - buildReport() combines the answers from the smaller methods into one
 *   TicketReport object.
 * - Use streams to express the data processing logic.
 * - Do not use loops.
 *
 * Edge cases to think about:
 * - What should the constructor do if the provided list is null?
 * - What should the average be if there are no resolved tickets?
 * - What should the category-count map look like if the ticket list is empty?
 * - Should callers be able to modify the list returned by
 *   getHighPriorityUnresolved()?
 * - Should callers be able to modify the map returned by getCountByCategory()?
 */
public class TicketReportBuilder {

    private final List<SupportTicket> tickets;

    /**
     * Store the tickets that this report builder will analyze.
     *
     * Think carefully about whether this constructor should keep the original
     * list reference or store a defensive copy.
     */
    public TicketReportBuilder(List<SupportTicket> tickets) {
        // TODO: validate and store the tickets this object will analyze
        // validate
        if (tickets == null ) {
            throw new IllegalArgumentException("tickets cannot be null");
        }
        //store a defensive copy so outside code cannot mutate this object
        this.tickets = List.copyOf(tickets);
    }

    /**
     * Return how many tickets in this report data set were resolved.
     */
    public long getResolvedCount() {
        // TODO: calculate from tickets
        return tickets.stream().filter(SupportTicket::resolved).count();
    }

    /**
     * Return the average resolution time for resolved tickets only.
     *
     * Tickets that are not resolved should not affect this average.
     */
    public double getAverageResolutionMinutes() {
        // TODO: calculate from tickets
        return tickets.stream()
                .filter(SupportTicket::resolved)
                .mapToDouble(SupportTicket::minutesToResolve)// specifically map to Double bc averaging numbers
                .average()
                .orElse(0.0); // this handles the case where there are no resolved tickets
    }

    /**
     * Return how many tickets belong to each category.
     */
    public Map<String, Long> getCountByCategory() {
        // TODO: calculate from tickets
        return Map.copyOf(tickets.stream()
                .collect(Collectors.groupingBy(SupportTicket::category, Collectors.counting())));
    }

    /**
     * Return unresolved tickets that should receive the most urgent attention.
     */
    public List<SupportTicket> getHighPriorityUnresolved() {
        // TODO: calculate from tickets
        return List.copyOf(tickets.stream()
                .filter(t -> !t.resolved())
                .filter(t -> t.priority() == Priority.HIGH)
                .collect(Collectors.toList()));
    }

    /**
     * Build one summary report by combining the smaller report questions above.
     */
    public TicketReport buildReport() {
        return new TicketReport(
                getResolvedCount(),
                getAverageResolutionMinutes(),
                getCountByCategory(),
                getHighPriorityUnresolved()
        );
    }
}
