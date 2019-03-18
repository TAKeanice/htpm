package de.dbvis.htpm.constraints;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultHTPMConstraint implements HTPMConstraint {

    /**
     * The minimum support each pattern has to satisfy
     */
    protected final double min_sup;

    /**
     * Database which is used for support calculation
     */
    private final HybridEventSequenceDatabase d;

    /**
     * The maximum allowed length for patterns
     */
    private final int maxPatternLength;

    public DefaultHTPMConstraint(HybridEventSequenceDatabase d, double minSupport, int maxPatternLength) {
        this.d = d;
        this.maxPatternLength = maxPatternLength;
        if(minSupport <= 0 || minSupport > 1) {
            throw new IllegalArgumentException("Minimum support must be 0 < min_support <= 1");
        }
        this.min_sup = minSupport;
    }

    @Override
    public boolean shouldGeneratePatternsOfLength(int k) {
        return k <= maxPatternLength;
    }

    public boolean patternsQualifyForJoin(HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k) {
        return firstPattern.getPrefix() == secondPattern.getPrefix();
    }

    public boolean occurrenceRecordsQualifyForJoin(Occurrence firstOccurrence, Occurrence secondOccurrence, int k) {
        //make sure it is valid to merge the two occurrence records: only if they have same prefix (hence also from same sequence)
        return firstOccurrence.getPrefix() == secondOccurrence.getPrefix();
    }

    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        //patterns have correct length automatically. Further, each occurrence is generated only once.
        return true;
    }

    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        //prune patterns which do not fulfill minimum support
        return this.isSupported(occurrences);
    }

    /**
     * Checks if a List of Occurrences has the minimum support
     * @param occurrences the Occurrences to check
     * @return true if minimum support is fulfilled, false otherwise
     */
    protected boolean isSupported(final List<Occurrence> occurrences) {
        return this.support(occurrences) >= this.min_sup;
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

        return ((double) sequenceIds.size()) / ((double) this.d.size());
    }
}
