package de.dbvis.htpm.constraints;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

public class MaxDurationConstraint extends DefaultHTPMConstraint {

    private final double maxDuration;

    public MaxDurationConstraint(HybridEventSequenceDatabase d, double minSupport, double maxDuration, int maxPatternLength) {
        super(d, minSupport, maxPatternLength);
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean occurrenceRecordsQualifyForJoin(Occurrence firstOccurrence, Occurrence secondOccurrence, int k) {
        //check second generation before join for too long intervals
        // afterwards only patterns with same prefix are joined and thus no too long patterns will be generated
        return super.occurrenceRecordsQualifyForJoin(firstOccurrence, secondOccurrence, k)
                && (k > 2 || !willExceedMaxDurationAfterJoin(firstOccurrence, secondOccurrence));
    }

    public boolean willExceedMaxDurationAfterJoin(Occurrence firstOccurrence, Occurrence secondOccurrence) {
        double start = Math.min(firstOccurrence.get(0).getTimePoint(), secondOccurrence.get(0).getTimePoint());
        double end = Math.max(firstOccurrence.get(firstOccurrence.size() - 1).getTimePoint(),
                secondOccurrence.get(secondOccurrence.size() - 1).getTimePoint());
        double duration = end - start;
        return duration > maxDuration;
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        //only check first generation for too long intervals
        return super.newOccurrenceFulfillsConstraints(pattern, occurrence, k)
                && (k > 1 || !this.isOverMaxDuration(occurrence));
    }

    private boolean isOverMaxDuration(Occurrence occurrence) {
        if(this.maxDuration <= 0) {
            return false;
        }

        double duration = occurrence.get(occurrence.size() - 1).getTimePoint() - occurrence.get(0).getTimePoint();

        return duration > maxDuration;
    }
}
