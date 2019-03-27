package de.dbvis.htpm.constraints;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public class EpisodeMiningConstraint extends MaxDurationConstraint implements SupportCounter {

    private final int minOccurrences;

    public EpisodeMiningConstraint(HybridEventSequenceDatabase d, int minOccurrences, double maxDuration, int maxPatternLength) {
        super(d, 0.5, maxDuration, maxPatternLength);
        if (minOccurrences <= 0) {
            throw new IllegalArgumentException("patterns must be required to occur at least once (0 < minOccurrences)");
        }
        this.minOccurrences = minOccurrences;
    }

    /**
     * checks if there are enough occurrences of that pattern
     */
    @Override
    public boolean isSupported(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        //The number of sequences is not important in episode mining,
        // rather the absolute number of occurrences.
        return occurrences.size() > minOccurrences;
    }
}
