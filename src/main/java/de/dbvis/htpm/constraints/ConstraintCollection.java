package de.dbvis.htpm.constraints;

import de.dbvis.htpm.HTPM;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;
import java.util.function.Predicate;

public class ConstraintCollection implements HTPMConstraint {

    private final List<HTPMConstraint> constraints;

    public ConstraintCollection(List<HTPMConstraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public boolean shouldGeneratePatternsOfLength(int k) {
        return passesAll(c -> c.shouldGeneratePatternsOfLength(k));
    }

    @Override
    public boolean patternsQualifyForJoin(HybridTemporalPattern commonPrefix, HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k) {
        return passesAll(c -> c.patternsQualifyForJoin(commonPrefix, firstPattern, secondPattern, k));
    }

    @Override
    public boolean occurrenceRecordsQualifyForJoin(Occurrence firstOccurrence, Occurrence secondOccurrence, int k) {
        return passesAll(c -> c.occurrenceRecordsQualifyForJoin(firstOccurrence, secondOccurrence, k));
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        return passesAll(c -> c.newOccurrenceFulfillsConstraints(pattern, occurrence, k));
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        return passesAll(c -> c.patternFulfillsConstraints(p, occurrences, k));
    }

    @Override
    public boolean branchCanProduceResults(List<HTPM.PatternOccurrence> patternsWithOccurrences) {
        return passesAll(c -> c.branchCanProduceResults(patternsWithOccurrences));
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        return passesAll(c -> c.shouldOutputOccurrence(p, occurrence));
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return passesAll(c -> c.shouldOutputPattern(p, occurrences));
    }

    private boolean passesAll(Predicate<HTPMConstraint> constraintPredicate) {
        for (HTPMConstraint c : constraints) {
            if (!constraintPredicate.test(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void foundPattern(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        for (HTPMConstraint c : constraints) {
            c.foundPattern(p, occurrences, k);
        }
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
        return 0;
    }

    @Override
    public int getPatternsDiscardedCount() {
        return 0;
    }

    @Override
    public int getBranchesCutCount() {
        return 0;
    }

    @Override
    public String toString() {
        return "Collection of conjunct constraints: " + constraints.toString();
    }
}
