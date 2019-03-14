package de.dbvis.htpm;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

public class MaxDurationConstraint extends DefaultHTPMConstraint {

    private final double maxDuration;

    public MaxDurationConstraint(HybridEventSequenceDatabase d, double min_sup, double maxDuration) {
        super(d, min_sup);
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        return super.newOccurrenceFulfillsConstraints(pattern, occurrence, k) && (k > 2 || !this.isOverMaxDuration(occurrence));
    }

    private boolean isOverMaxDuration(Occurrence occurrence) {
        if(this.maxDuration <= 0) {
            return false;
        }

        double duration = occurrence.get(occurrence.size() - 1).getTimePoint() - occurrence.get(0).getTimePoint();

        return duration > maxDuration;
    }
}
