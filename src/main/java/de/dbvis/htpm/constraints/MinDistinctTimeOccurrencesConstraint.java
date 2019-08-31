package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MinDistinctTimeOccurrencesConstraint extends AcceptAllConstraint implements SupportBasedConstraint {

    private final int minMinimalOccurrences;

    private int patternsDiscardedCount = 0;
    private int occurrencesDiscardedCount = 0;

    public MinDistinctTimeOccurrencesConstraint(int minMinimalOccurrences) {
        this.minMinimalOccurrences = minMinimalOccurrences;
    }

    //TODO: minimal occurrences counting --> is it valid to use maximal number of nonoverlapping intervals algorighm?


    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, Set<Occurrence> occurrences, int k) {
        final boolean supported = isSupported(p, occurrences);
        if (!supported) {
            patternsDiscardedCount++;
            occurrencesDiscardedCount += occurrences.size();
        }
        return supported;
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        return false;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        return isSupported(p, occurrences);
    }

    @Override
    public int getPatternJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrenceJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return 0;
    }

    @Override
    public int getPatternsDiscardedCount() {
        return 0;
    }

    @Override
    public int getBranchesCutCount() {
        return 0;
    }

    private boolean isSupported(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        int numDistinct = (int) getSupport(p, occurrences);
        return numDistinct >= minMinimalOccurrences;
    }

    @Override
    public double getSupport(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        return calculateOccurrences(p, occurrences);
    }

    /**
     * Calculates support by considering how many occurrences are there that do not overlap temporally
     * @param p the pattern
     * @param occurrences occurrences of the pattern
     * @return the maximum number of temporally non-overlapping occurrences
     */
    public static double calculateOccurrences(HybridTemporalPattern p, Set<Occurrence> occurrences) {

        Collection<List<Occurrence>> occurrencesInSequences = occurrences.stream()
                .collect(Collectors.groupingBy(Occurrence::getHybridEventSequence)).values();

        // (end, start) pairs sorted by end
        List<List<Pair<Double, Double>>> occurrenceIntervalLists = occurrencesInSequences.stream()
                .map(occurrenceSet -> occurrenceSet.stream()
                        .map(occ -> Pair.of(
                                Occurrence.getTimepointOfOccurrencePoint(p.getEventNode(p.size() - 1), occ.get(occ.size() - 1)),
                                Occurrence.getTimepointOfOccurrencePoint(p.getEventNode(0), occ.get(0))
                        )).sorted().collect(Collectors.toList())
                ).collect(Collectors.toList());

        //use greedy algorithm to get the maximum non-overlapping intervals count per sequence and add all results up.
        // the resulting number is usable as apriori property, since patterns cannot grow shorter
        // and occurrences removed by other constraints or in joins cannot increase but only decrease the number
        // because either the removed occurrence counted (decreases number)
        // or does not remove overlaps that allow other occurrences to be counted (keeps number the same)

        int numDistinct = 0;
        for (List<Pair<Double, Double>> occurrenceIntervals : occurrenceIntervalLists) {
            double currentTime = Double.NEGATIVE_INFINITY;
            for (Pair<Double, Double> occurrenceInterval : occurrenceIntervals) {
                if (occurrenceInterval.getRight() > currentTime) {
                    numDistinct++;
                    currentTime = occurrenceInterval.getLeft();
                }
            }
        }
        return numDistinct;
    }

    @Override
    public String toString() {
        return "Min " + minMinimalOccurrences + " minimal occurrences constraint, all combinations count " +
                "(discarded " + patternsDiscardedCount + " patterns with a total of " + occurrencesDiscardedCount + " occurrences)";
    }
}
