package de.dbvis.htpm.constraints;

import de.dbvis.htpm.PatternOccurrence;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;
import java.util.Set;

public abstract class AcceptAllConstraint implements HTPMConstraint {
    @Override
    public boolean shouldGeneratePatternsOfLength(int k) {
        return true;
    }

    @Override
    public boolean patternsQualifyForJoin(HybridTemporalPattern commonPrefix, HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k) {
        return true;
    }

    @Override
    public boolean occurrenceRecordsQualifyForJoin(HybridTemporalPattern firstPattern, Occurrence firstOccurrence, HybridTemporalPattern secondPattern, Occurrence secondOccurrence, int k) {
        return true;
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        return true;
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, Set<Occurrence> occurrences, int k) {
        return true;
    }

    @Override
    public boolean branchCanProduceResults(List<PatternOccurrence> patternsWithOccurrences) {
        return true;
    }

    @Override
    public void foundPattern(HybridTemporalPattern p, Set<Occurrence> occurrences, int k) { }
}
