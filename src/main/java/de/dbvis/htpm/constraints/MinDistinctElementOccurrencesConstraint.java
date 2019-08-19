package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;
import java.util.stream.IntStream;

public class MinDistinctElementOccurrencesConstraint extends AcceptAllConstraint {
    private final int minOccurrences;

    private int patternsDiscardedCount = 0;
    private int occurrencesDiscardedCount = 0;

    public MinDistinctElementOccurrencesConstraint(int minOccurrences) {
        if (minOccurrences <= 0) {
            throw new IllegalArgumentException("patterns must be required to occur at least once (0 < minOccurrences)");
        }
        this.minOccurrences = minOccurrences;
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        final boolean supported = isSupported(occurrences);
        if (!supported) {
            patternsDiscardedCount++;
            occurrencesDiscardedCount += occurrences.size();
        }
        return supported;
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        return true;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return isSupported(occurrences);
    }

    @Override
    public int getPatternsDiscardedCount() {
        return patternsDiscardedCount;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return occurrencesDiscardedCount;
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
    public int getBranchesCutCount() {
        return 0;
    }

    /**
     * checks if there are enough occurrences of that pattern
     */
    public boolean isSupported(List<Occurrence> occurrences) {
        if (occurrences.size() < minOccurrences) {
            //we do not need to examine occurrences further if there are too few anyway
            return false;
        }

        //There can only be as many distinct occurrences as the minimum of distinct events in one slot in the pattern
        //It may still overestimate the number of occurrences, but it does fulfill the apriori-property.
        //Because by the mechanism of joining patterns,
        // it is not possible to increase the minimum number of distinct events in one slot in the pattern!
        int minNumber = IntStream.range(0, occurrences.get(0).size()).map(i ->
                (int) occurrences.stream().map(occ -> occ.get(i)).distinct().count() //count distinct elements for slot i
        ).min().orElseThrow(); //take minimum of distinct elements (stream is not empty, so it never fails)
        return minNumber >= minOccurrences;
    }

    @Override
    public String toString() {
        return "Min " + minOccurrences + " distinct element occurrences constraint, all combinations count " +
                "(discarded " + patternsDiscardedCount + " patterns with a total of " + occurrencesDiscardedCount + " occurrences)";
    }
}
