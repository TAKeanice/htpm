package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public class MinCombinatorialOccurrencesConstraint extends AcceptAllConstraint {

    private final int minOccurrences;

    public MinCombinatorialOccurrencesConstraint(int minOccurrences) {
        if (minOccurrences <= 0) {
            throw new IllegalArgumentException("patterns must be required to occur at least once (0 < minOccurrences)");
        }
        this.minOccurrences = minOccurrences;
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
        return 0;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return 0;
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
    public boolean isSupported(List<?> occurrences) {
        return occurrences.size() > minOccurrences;
    }

    @Override
    public String toString() {
        return "Min " + minOccurrences + " combinatorial occurrences constraint, all combinations count";
    }
}
