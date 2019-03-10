package de.dbvis.htpm;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

/**
 * This is an extension of the HTPM Algorithm.
 * It introduces the max-gap constraint in order to reduce the result set.
 * Created by Wolfgang Jentner <wolfgang.jentner@uni.kn> on 12/17/2015 10:55 AM.
 */
public class eHTPM extends HTPM {

    private int maxDuration;

    /**
     * Creates a new HTPM-Algorithm-Object.
     *
     * @param d           - The Database containing the series.
     * @param min_support - The minimum support.
     * @param maxDuration      - The max gap constraint, if 0 maxDuration will be ignored.
     */
    public eHTPM(HybridEventSequenceDatabase d, double min_support, int maxDuration) {
        super(d, min_support);
        this.maxDuration = maxDuration;
    }

    public int getMaxDuration() {
        return this.maxDuration;
    }

    @Override
    protected boolean newOccurrencePasses(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        return super.newOccurrencePasses(pattern, occurrence, k) && (k > 2 || !this.isOverMaxDuration(occurrence));
    }

    protected boolean isOverMaxDuration(Occurrence occurrence) {
        if(this.maxDuration <= 0) {
            return false;
        }

        double duration = occurrence.get(occurrence.size() - 1).getTimePoint() - occurrence.get(0).getTimePoint();

        return duration > maxDuration;
    }

}
