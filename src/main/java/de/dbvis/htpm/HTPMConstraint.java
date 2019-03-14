package de.dbvis.htpm;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public interface HTPMConstraint {
    boolean patternsQualifyForJoin(HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern);
    boolean occurrenceRecordsQualifyForJoin(Occurrence firstOccurrence, Occurrence secondOccurrence);
    boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k);
    boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences);
}
