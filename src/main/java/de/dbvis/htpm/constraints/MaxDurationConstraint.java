package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

public class MaxDurationConstraint extends AcceptAllConstraint {

    private final double maxDuration;

    private int occurrenceJoinPreventedCount;
    private int occurrencesDiscardedCount;

    public MaxDurationConstraint(double maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean occurrenceRecordsQualifyForJoin(Occurrence firstOccurrence, Occurrence secondOccurrence, int k) {
        //check second generation before join for too long intervals
        // afterwards only patterns with same prefix are joined and thus no too long patterns will be generated
        final boolean join = k > 2 || !willExceedMaxDurationAfterJoin(firstOccurrence, secondOccurrence);
        if (!join) {
            occurrenceJoinPreventedCount++;
        }
        return join;
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        //only check first generation for too long intervals
        final boolean fulfills = k > 1 || !this.isOverMaxDuration(occurrence);
        if (!fulfills) {
            occurrencesDiscardedCount++;
        }
        return fulfills;
    }

    @Override
    public int getPatternJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrenceJoinPreventedCount() {
        return occurrenceJoinPreventedCount;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return occurrencesDiscardedCount;
    }

    @Override
    public int getPatternsDiscardedCount() {
        return 0;
    }

    private boolean willExceedMaxDurationAfterJoin(Occurrence firstOccurrence, Occurrence secondOccurrence) {
        double start = Math.min(firstOccurrence.get(0).getTimePoint(), secondOccurrence.get(0).getTimePoint());
        double end = Math.max(firstOccurrence.get(firstOccurrence.size() - 1).getTimePoint(),
                secondOccurrence.get(secondOccurrence.size() - 1).getTimePoint());
        double duration = end - start;
        return duration > maxDuration;
    }

    private boolean isOverMaxDuration(Occurrence occurrence) {
        if(this.maxDuration <= 0) {
            return false;
        }

        double duration = occurrence.get(occurrence.size() - 1).getTimePoint() - occurrence.get(0).getTimePoint();

        return duration > maxDuration;
    }
}
