package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.Set;

public class AgrawalSupportConstraint extends AcceptAllConstraint implements SupportBasedConstraint {

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

    public AgrawalSupportConstraint(int numSequences, double minSupport) {
        this.numSequences = numSequences;
        if(minSupport <= 0 || minSupport > 1) {
            throw new IllegalArgumentException("Minimum support must be 0 < min_support <= 1");
        }
        this.minSupport = minSupport;
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, Set<Occurrence> occurrences, int k) {
        //prune patterns which do not fulfill minimum support
        boolean isSupported = isSupported(occurrences);
        if (!isSupported) {
            unsupportedCount++;
            unsupportedOccurrences += occurrences.size();
        }
        return isSupported;
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        return true;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, Set<Occurrence> occurrences) {
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

    @Override
    public int getBranchesCutCount() {
        return 0;
    }

    /**
     * Checks if there are enough sequences supporting the pattern
     */
    private boolean isSupported(final Set<Occurrence> occurrences) {
        return calculateSupport(occurrences, numSequences) >= this.minSupport;
    }

    @Override
    public double getSupport(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        return calculateSupport(occurrences, numSequences);
    }

    /**
     * Returns the support (after the original definition by Agrawal) of a list of occurrences of some pattern
     * @param occurrences occurrences of the pattern
     * @param numSequences the total number of sequences in the database
     * @return the support
     */
    public static double calculateSupport(Set<Occurrence> occurrences, double numSequences) {
        //support is based on the number of sequences in which the pattern occurs
        long numSequencesWithOccurrence = occurrences.stream().map(Occurrence::getHybridEventSequence).distinct().count();
        return numSequencesWithOccurrence / numSequences;
    }


    @Override
    public String toString() {
        return "MinSupport " + String.format("%.3f", minSupport) + " constraint " +
                "(discarded " + unsupportedCount + " patterns)";
    }
}
