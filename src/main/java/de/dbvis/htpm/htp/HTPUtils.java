package de.dbvis.htpm.htp;

import de.dbvis.htpm.htp.eventnodes.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Comparator;
import java.util.List;

/**
 * collection of static helper methods for Hybrid Temporal Patterns
 */
public final class HTPUtils {

    private HTPUtils() {}

    public static int getLastIndexOfUnmodifiablePart(List<EventNode> nodes, List<IntervalEndEventNode> openEnds) {
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
        return secondLastStart;
    }



    public static int compare(HybridTemporalPattern first, HybridTemporalPattern second) {
        List<HTPItem> firstItems = first.getPatternItemsInStringIdOrder();
        List<HTPItem> secondItems = second.getPatternItemsInStringIdOrder();
        int result = Integer.compare(first.length(), second.length());
        for (int i = 0; i < Math.min(firstItems.size(), secondItems.size()) && result == 0; i++) {
            if (firstItems.get(i) instanceof EventNode) {
                result = EventNode.compareByStringId((EventNode) firstItems.get(i), (EventNode) secondItems.get(i));
            } else {
                result = ((OrderRelation) firstItems.get(i)).compareTo((OrderRelation) secondItems.get(i));
            }
        }
        return result != 0 ? result : Integer.compare(firstItems.size(), secondItems.size());
    }

    public static void sortItemsets(List<EventNode> eventnodes, List<OrderRelation> orderrelations, Comparator<EventNode> c) {
        //even for correctly specified patterns, orders within a group connected with "=" signs (e.g. a=b=c)
        //might be different internally, because we use IDs to order nodes instead of string order (e.g. a=c=b)
        int groupStart = 0;
        for (int i = 0; i <= orderrelations.size(); i++) {
            if (i == orderrelations.size() || orderrelations.get(i) == OrderRelation.SMALLER) {
                //we finished a group. Sort group internally.
                eventnodes.subList(groupStart, i + 1).sort(c);
                groupStart = i + 1;
            }
        }
    }

    public static boolean equal(HybridTemporalPattern htp1, HybridTemporalPattern htp2) {
        return htp1.getPatternItemsInIntegerIdOrder().equals(htp2.getPatternItemsInIntegerIdOrder());
    }

    public static int hashCode(HybridTemporalPattern pattern) {
        return new HashCodeBuilder(13, 31)
                .append(pattern.getPatternItemsInIntegerIdOrder())
                .hashCode();
    }
}
