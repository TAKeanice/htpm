package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public class PatternSizeConstraint extends AcceptAllConstraint {

    /**
     * The maximum allowed length for patterns
     */
    private final int maxPatternLength;

    /**
     * the minimum length for patterns to be output
     */
    private final int minOutputPatternLength;

    public PatternSizeConstraint(int maxPatternLength, int minOutputPatternLength) {
        this.maxPatternLength = maxPatternLength;
        this.minOutputPatternLength = minOutputPatternLength;
    }

    @Override
    public boolean shouldGeneratePatternsOfLength(int k) {
        return k <= maxPatternLength;
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        return true;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return minOutputPatternLength <= p.length() && p.length() <= maxPatternLength;
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

    @Override
    public String toString() {
        return "Pattern size " + minOutputPatternLength + " - " + maxPatternLength + " constraint";
    }
}
