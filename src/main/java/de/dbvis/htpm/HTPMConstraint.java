package de.dbvis.htpm;

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
     * @param firstPattern the first pattern in the upcoming join
     * @param secondPattern the second pattern in the upcoming join
     * @param k length of patterns that shall be generated
     * @return whether the two patterns should be joined
     */
    boolean patternsQualifyForJoin(HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k);

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
}
