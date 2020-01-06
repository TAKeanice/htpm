package de.dbvis.htpm.constraints;

import de.dbvis.htpm.PatternOccurrence;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;
import java.util.Set;

public class ConstraintCollection implements HTPMConstraint {

    private final List<HTPMConstraint> constraints;

    public ConstraintCollection(List<HTPMConstraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public boolean shouldGeneratePatternsOfLength(int k) {
        for (HTPMConstraint c1 : constraints) {
            if (!c1.shouldGeneratePatternsOfLength(k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean patternsQualifyForJoin(HybridTemporalPattern commonPrefix, HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k) {
        for (HTPMConstraint c1 : constraints) {
            if (!c1.patternsQualifyForJoin(commonPrefix, firstPattern, secondPattern, k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean occurrenceRecordsQualifyForJoin(HybridTemporalPattern firstPattern, Occurrence firstOccurrence, HybridTemporalPattern secondPattern, Occurrence secondOccurrence, int k) {
        for (HTPMConstraint c1 : constraints) {
            if (!c1.occurrenceRecordsQualifyForJoin(firstPattern, firstOccurrence, secondPattern, secondOccurrence, k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        for (HTPMConstraint c1 : constraints) {
            if (!c1.newOccurrenceFulfillsConstraints(pattern, occurrence, k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, Set<Occurrence> occurrences, int k) {
        for (HTPMConstraint c1 : constraints) {
            if (!c1.patternFulfillsConstraints(p, occurrences, k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean branchCanProduceResults(List<PatternOccurrence> patternsWithOccurrences) {
        for (HTPMConstraint c1 : constraints) {
            if (!c1.branchCanProduceResults(patternsWithOccurrences)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        for (HTPMConstraint c1 : constraints) {
            if (!c1.shouldOutputOccurrence(p, occurrence)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, Set<Occurrence> occurrences) {
        for (HTPMConstraint c1 : constraints) {
            if (!c1.shouldOutputPattern(p, occurrences)) {
                return false;
            }
        }
        return true;
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
