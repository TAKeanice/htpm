package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public class MinOccurrencesConstraint extends AcceptAllConstraint {

    private final int minOccurrences;

    private int patternsDiscardedCount;
    private int occurrencesDiscardedCount;

    public MinOccurrencesConstraint(int minOccurrences) {
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
    public int getPatternsDiscardedCount() {
        return patternsDiscardedCount;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return occurrencesDiscardedCount;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return isSupported(occurrences);
    }

    @Override
    public int getPatternJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrenceJoinPreventedCount() {
        return 0;
    }

    /**
     * checks if there are enough occurrences of that pattern
     */
    public boolean isSupported(List<Occurrence> occurrences) {
        //The number of sequences is not important in episode mining,
        // rather the absolute number of occurrences.
        return occurrences.size() > minOccurrences;
    }

    @Override
    public String toString() {
        return "Min " + minOccurrences + " occurrences episode mining constraint " +
                "(discarded " + patternsDiscardedCount + " patterns)";
    }
}
