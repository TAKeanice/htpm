package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalEndEventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalStartEventNode;
import de.dbvis.htpm.htp.eventnodes.PointEventNode;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.ArrayList;
import java.util.List;

/**
 * this is an adaptation of the maxGap constraint.
 * It only applies to gaps in the PREFIX of the pattern.
 * The constraint does not restrict gaps between the prefix and the suffix of the pattern,
 * because that could rule out patterns with gaps that could be filled in later joins with other patterns.
 */
public class PrefixMaxGapConstraint extends AcceptAllConstraint {

    private final double maxGap;

    private int occurrencesDiscardedCount;

    public PrefixMaxGapConstraint(double maxGap) {
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

        boolean foundLastStart = false;
        int secondLastStart = -1;

        int i = nodes.size();
        while (secondLastStart < 0 && i > 0) {
            i--;
            final EventNode currentNode = nodes.get(i);

            if (currentNode instanceof IntervalEndEventNode) {
                openEnds.add((IntervalEndEventNode) currentNode);
            } else if (currentNode instanceof IntervalStartEventNode || currentNode instanceof PointEventNode) {

                if (currentNode instanceof IntervalStartEventNode) {
                    //some open end is now closed
                    IntervalStartEventNode startNode = (IntervalStartEventNode) currentNode;
                    openEnds.removeIf(node -> node.id == startNode.id && node.occurrencemark == startNode.occurrencemark);
                }

                if (!foundLastStart) {
                    foundLastStart = true;
                } else {
                    secondLastStart = i;
                }
            }
        }

        if (!openEnds.isEmpty()) {
            //there are events overlapping the second last start
            //thus we can say for sure there is no gap in the prefix
            return true;
        }

        //there can be a gap between the start or point node just added to the prefix and the node before.
        //all gaps before that are ruled out from being too large, because this constraint was already applied.
        double gap = occurrence.get(secondLastStart).getTimePoint() - occurrence.get(secondLastStart - 1).getTimePoint();

        if (gap > maxGap) {
            occurrencesDiscardedCount++;
            return false;
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
        return occurrencesDiscardedCount;
    }

    @Override
    public int getPatternsDiscardedCount() {
        return 0;
    }
}
