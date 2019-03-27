package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public class ConstraintCollection implements HTPMConstraint {

    private final List<HTPMConstraint> constraints;

    public ConstraintCollection(List<HTPMConstraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public boolean shouldGeneratePatternsOfLength(int k) {
        for (HTPMConstraint c : constraints) {
            if (!c.shouldGeneratePatternsOfLength(k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean patternsQualifyForJoin(HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k) {
        for (HTPMConstraint c : constraints) {
            if (!c.patternsQualifyForJoin(firstPattern, secondPattern, k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean occurrenceRecordsQualifyForJoin(Occurrence firstOccurrence, Occurrence secondOccurrence, int k) {
        for (HTPMConstraint c : constraints) {
            if (!c.occurrenceRecordsQualifyForJoin(firstOccurrence, secondOccurrence, k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        for (HTPMConstraint c : constraints) {
            if (!c.newOccurrenceFulfillsConstraints(pattern, occurrence, k)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        for (HTPMConstraint c : constraints) {
            if (!c.patternFulfillsConstraints(p, occurrences, k)) {
                return false;
            }
        }
        return true;
    }
}
