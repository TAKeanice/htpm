package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HTPUtils;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalEndEventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalStartEventNode;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * this is an adaptation of the maxGap constraint.
 * It only applies to gaps in the PREFIX of the pattern.
 * The constraint does not restrict gaps between the prefix and the suffix of the pattern,
 * because that could rule out patterns with gaps that could be filled in later joins with other patterns.
 */
public class MaxGapConstraint extends AcceptAllConstraint {

    private final double maxGap;

    private int occurrencesDiscardedCount;

    public MaxGapConstraint(double maxGap) {
        if (maxGap < 0) {
            throw new IllegalArgumentException("maxGap must be positive!");
        }
        this.maxGap = maxGap;
    }

    @Override
    public boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {

        if (k <= 2) {
            return true;
        }

        final List<EventNode> nodes = pattern.getEventNodes();

        List<IntervalEndEventNode> openEnds = new ArrayList<>(k);

        int secondLastStart = HTPUtils.getLastIndexOfUnmodifiablePart(nodes, openEnds);

        if (!openEnds.isEmpty()) {
            //there are events overlapping the second last start
            //thus we can say for sure there is no gap in the prefix
            return true;
        }

        //there can be a gap between the start or point node just added to the prefix and the node before.
        //all gaps before that are ruled out from being too large, because this constraint was already applied.
        double gapStart = Occurrence.getTimepoint(pattern, occurrence, secondLastStart - 1);
        double gapEnd = Occurrence.getTimepoint(pattern, occurrence, secondLastStart);
        double gap = gapEnd - gapStart;

        if (gap > maxGap) {
            occurrencesDiscardedCount++;
            return false;
        }

        return true;
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        List<EventNode> nodes = p.getEventNodes();
        List<Integer> openIntervals = new ArrayList<>();
        double gapStart = Double.MAX_VALUE;
        for (int i = 0; i < occurrence.size(); i++) {

            double gapEnd = Occurrence.getTimepoint(p, occurrence, i);
            if (openIntervals.isEmpty()) {
                //too large gap detected (no open intervals span gap between last node and this one)
                if (gapEnd - gapStart > maxGap) {
                    return false;
                }
            }
            gapStart = gapEnd;

            if (nodes.get(i) instanceof IntervalStartEventNode) {
                openIntervals.add(nodes.get(i).id);
            } else if (nodes.get(i) instanceof IntervalEndEventNode) {
                openIntervals.remove((Integer) nodes.get(i).id);
            }
        }
        //did not find too large gap
        return true;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, Set<Occurrence> occurrences) {
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

    @Override
    public String toString() {
        return "Max gap " + maxGap + " constraint " +
                "(" + occurrencesDiscardedCount + " occurrences discarded)";
    }
}
