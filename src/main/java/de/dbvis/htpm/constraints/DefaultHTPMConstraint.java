package de.dbvis.htpm.constraints;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultHTPMConstraint extends AcceptAllConstraint implements SupportCounter {

    /**
     * The minimum support each pattern has to satisfy
     */
    protected final double minSupport;

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
        this.minSupport = minSupport;
    }

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

    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        //prune patterns which do not fulfill minimum support
        return this.isSupported(p, occurrences, k);
    }

    public long unsupportedCount = 0;
    public long unsupportedOccurrences = 0;
    public long supportedCount = 0;
    public long supportedOccurrences = 0;

    /**
     * Checks if there are enough sequences supporting the pattern
     */
    public boolean isSupported(HybridTemporalPattern p, final List<Occurrence> occurrences, int k) {
        final boolean isSupported = this.support(occurrences) >= this.minSupport;
        if (!isSupported) {
            unsupportedCount++;
            unsupportedOccurrences += occurrences.size();
        } else {
            supportedCount++;
            supportedOccurrences += occurrences.size();
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

        return ((double) sequenceIds.size()) / ((double) this.d.size());
    }
}
