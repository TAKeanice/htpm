package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultHTPMConstraint extends AcceptAllConstraint {

    /**
     * The minimum support each pattern has to satisfy
     */
    protected final double minSupport;

    /**
     * Number of sequences in the database
     */
    private final double numSequences;

    private int unsupportedCount = 0;
    private int unsupportedOccurrences = 0;

    public DefaultHTPMConstraint(int numSequences, double minSupport) {
        this.numSequences = numSequences;
        if(minSupport <= 0 || minSupport > 1) {
            throw new IllegalArgumentException("Minimum support must be 0 < min_support <= 1");
        }
        this.minSupport = minSupport;
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        //prune patterns which do not fulfill minimum support
        return isSupported(occurrences);
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
    public int getPatternJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrenceJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return unsupportedOccurrences;
    }

    @Override
    public int getPatternsDiscardedCount() {
        return unsupportedCount;
    }

    /**
     * Checks if there are enough sequences supporting the pattern
     */
    public boolean isSupported(final List<Occurrence> occurrences) {
        final boolean isSupported = this.support(occurrences) >= this.minSupport;
        if (!isSupported) {
            unsupportedCount++;
            unsupportedOccurrences += occurrences.size();
        }
        return isSupported;
    }

    /**
     * Returns the support of a List of Occurrences
     * @param occurrences the occurrences
     * @return the support
     */
    protected double support(final List<Occurrence> occurrences) {
        Set<String> sequenceIds = new HashSet<>();

        //support is implicitly counted by joining all joinable occurrence records once
        for(final Occurrence o : occurrences) {
            sequenceIds.add(o.getHybridEventSequence().getSequenceId());
        }

        return ((double) sequenceIds.size()) / numSequences;
    }


    @Override
    public String toString() {
        return "MinSupport " + String.format("%.3f", minSupport) + " constraint " +
                "(discarded " + unsupportedCount + " patterns)";
    }
}
