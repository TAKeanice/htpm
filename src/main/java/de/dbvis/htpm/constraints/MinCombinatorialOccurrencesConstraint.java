package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public class MinCombinatorialOccurrencesConstraint extends AcceptAllConstraint implements SupportBasedConstraint {

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
        return isSupported(p, occurrences);
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
    private boolean isSupported(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return getSupport(p, occurrences) > minOccurrences;
    }

    @Override
    public double getSupport(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return calculateOccurrences(p, occurrences);
    }

    /**
     * Calculates the number of occurrences by considering all occurrences (combinatorial counting without exclusions)
     * @param p the pattern
     * @param occurrences occurrences of the pattern
     * @return the number of occurrences
     */
    public static double calculateOccurrences(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return occurrences.size();
    }

    @Override
    public String toString() {
        return "Min " + minOccurrences + " combinatorial occurrences constraint, all combinations count";
    }
}
