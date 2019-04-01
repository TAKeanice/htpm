package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public interface HTPMConstraint {

    /**
     * for early stop
     * @param k the pattern length of the generation which is about to be generated
     * @return whether that generation should be generated
     */
    boolean shouldGeneratePatternsOfLength(int k);

    /**
     * for checking joinability of two patterns (e.g. prefix property)
     *
     * @param commonPrefix the prefix that both patterns have in common
     * @param firstPattern the first pattern in the upcoming join
     * @param secondPattern the second pattern in the upcoming join
     * @param k length of patterns that shall be generated
     * @return whether the two patterns should be joined
     */
    boolean patternsQualifyForJoin(HybridTemporalPattern commonPrefix, HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k);

    /**
     * for checking joinability of two occurrence records, e.g. if they stem from the same sequence
     * @param firstOccurrence the first occurrence in the upcoming join
     * @param secondOccurrence the second occurrence in the upcoming join
     * @param k the length of patterns and according occurrence records that are supposed to come up by joining
     * @return whether the two occurrence records should be joined
     */
    boolean occurrenceRecordsQualifyForJoin(Occurrence firstOccurrence, Occurrence secondOccurrence, int k);

    /**
     * whether the joined occurrence lies within the desired parameters
     * @param pattern the created pattern
     * @param occurrence the joined occurrence
     * @param k the desired length of pattern that is currently being created
     * @return whether the occurrence record is within the constraints or should be pruned
     */
    boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k);

    /**
     * whether the found pattern lies within the desired parameters (e.g. support threshold)
     * @param p the created pattern
     * @param occurrences all occurrences of pattern p
     * @param k the desired length of the patterns that have been created
     * @return whether the pattern p is within the constraints or should be pruned
     */
    boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k);

    /**
     * This is the only constraint method that does not need to be anti-monotone:
     * It is applied only when patterns are output, the rejected patterns are still used in the mining process.
     * @param p the pattern output candidate
     * @param occurrences occurrences of the output candidate
     * @return whether the pattern p and its occurrences shall be output
     */
    boolean shouldOutput(HybridTemporalPattern p, List<Occurrence> occurrences);

    /**
     * called whenever algorithm finds a hybrid temporal pattern,
     * in case the constraint needs to keep track of previously found patterns
     * @param p the found pattern
     * @param occurrences the occurrences of pattern p
     * @param k the length of pattern p
     */
    void foundPattern(HybridTemporalPattern p, List<Occurrence> occurrences, int k);

    int getPatternJoinPreventedCount();
    int getOccurrenceJoinPreventedCount();
    int getOccurrencesDiscardedCount();
    int getPatternsDiscardedCount();
}
