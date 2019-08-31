package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class MinDistinctElementOccurrencesConstraint extends AcceptAllConstraint implements SupportBasedConstraint {
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
        return true;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        return isSupported(p, occurrences);
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
    private boolean isSupported(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        if (occurrences.size() < minOccurrences) {
            //we do not need to examine occurrences further if there are too few anyway
            return false;
        }

        return getSupport(p, occurrences) >= minOccurrences;
    }

    @Override
    public double getSupport(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        return calculateOccurrences(p, occurrences);
    }

    /**
     * Counts support by the minimum number of distinct elements in one slot of the pattern
     * @param p the pattern
     * @param occurrences the occurrences of the pattern
     * @return the number of occurrences of a pattern
     */
    public static double calculateOccurrences(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        //There can only be as many distinct occurrences as the minimum of distinct events in one slot in the pattern
        //It may still overestimate the number of occurrences, but it does fulfill the apriori-property.
        //Because by the mechanism of joining patterns,
        // it is not possible to increase the minimum number of distinct events in one slot in the pattern!
        return IntStream.range(0, p.size()).map(i ->
                (int) occurrences.stream().map(occ -> occ.get(i)).distinct().count()) //count distinct elements for slot i
                .min().orElse(0); //0 if we put in an empty occurrence list
    }

    @Override
    public String toString() {
        return "Min " + minOccurrences + " distinct element occurrences constraint, all combinations count " +
                "(discarded " + patternsDiscardedCount + " patterns with a total of " + occurrencesDiscardedCount + " occurrences)";
    }
}
