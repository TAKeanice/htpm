package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public class EpisodeMiningConstraint extends MaxDurationConstraint {

    private final int minOccurrences;

    private int patternsDiscardedCount;
    private int occurrencesDiscardedCount;

    public EpisodeMiningConstraint(int minOccurrences, double maxDuration) {
        super(maxDuration);
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
    public int getPatternsDiscardedCount() {
        return patternsDiscardedCount;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return occurrencesDiscardedCount;
    }

    /**
     * checks if there are enough occurrences of that pattern
     */
    public boolean isSupported(List<Occurrence> occurrences) {
        //The number of sequences is not important in episode mining,
        // rather the absolute number of occurrences.
        return occurrences.size() > minOccurrences;
    }
}
