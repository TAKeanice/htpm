package de.dbvis.htpm.constraints;

import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public class MaxDurationConstraint extends AcceptAllConstraint {

    private final double maxDuration;

    private int occurrenceJoinPreventedCount;
    private int occurrencesDiscardedCount;

    public MaxDurationConstraint(double maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean occurrenceRecordsQualifyForJoin(HybridTemporalPattern firstPattern, Occurrence firstOccurrence,
                                                   HybridTemporalPattern secondPattern, Occurrence secondOccurrence,
                                                   int k) {
        //check second generation before join for too long intervals
        // afterwards only patterns with same prefix are joined and thus no too long patterns will be generated
        final boolean join = k > 2
                || !willExceedMaxDurationAfterJoin(firstPattern, firstOccurrence, secondPattern, secondOccurrence);
        if (!join) {
            occurrenceJoinPreventedCount++;
        }
        return join;
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
        //only check first generation for too long intervals
        final boolean fulfills = k > 1 || this.isUnderMaxDuration(pattern, occurrence);
        if (!fulfills) {
            occurrencesDiscardedCount++;
        }
        return fulfills;
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        return isUnderMaxDuration(p, occurrence);
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return true;
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

    @Override
    public int getBranchesCutCount() {
        return 0;
    }

    private boolean willExceedMaxDurationAfterJoin(HybridTemporalPattern firstPattern, Occurrence firstOccurrence,
                                                   HybridTemporalPattern secondPattern, Occurrence secondOccurrence) {
        EventNode sn1 = firstPattern.getEventNode(0);
        EventNode sn2 = secondPattern.getEventNode(0);

        EventNode en1 = firstPattern.getEventNode(firstPattern.size());
        EventNode en2 = secondPattern.getEventNode(secondPattern.size());

        final HybridEvent se1 = firstOccurrence.get(0);
        double s1 = Occurrence.getTimepointOfOccurrencePoint(sn1, se1);
        final HybridEvent se2 = secondOccurrence.get(0);
        double s2 = Occurrence.getTimepointOfOccurrencePoint(sn2, se2);

        final HybridEvent ee1 = firstOccurrence.get(firstOccurrence.size() - 1);
        double e1 = Occurrence.getTimepointOfOccurrencePoint(en1, ee1);
        final HybridEvent ee2 = secondOccurrence.get(secondOccurrence.size() - 1);
        double e2 = Occurrence.getTimepointOfOccurrencePoint(en2, ee2);

        double start = Math.min(s1, s2);
        double end = Math.max(e1, e2);
        double duration = end - start;
        return duration > maxDuration;
    }

    private boolean isUnderMaxDuration(HybridTemporalPattern pattern, Occurrence occurrence) {
        if(this.maxDuration <= 0) {
            return true;
        }

        final double endTime = Occurrence.getTimepointOfOccurrencePoint(
                pattern.getEventNode(pattern.size() - 1),
                occurrence.get(occurrence.size() - 1));
        final double start = Occurrence.getTimepointOfOccurrencePoint(pattern.getEventNode(0), occurrence.get(0));
        double duration = endTime - start;

        return duration <= maxDuration;
    }

    @Override
    public String toString() {
        return "Max duration " + String.format("%.3f", maxDuration) + " constraint " +
                "(" + occurrenceJoinPreventedCount + " occ. joins prevented, " +
                occurrencesDiscardedCount + " occurrences discarded)";
    }
}
