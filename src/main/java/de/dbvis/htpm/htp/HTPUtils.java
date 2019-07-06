package de.dbvis.htpm.htp;

import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.eventnodes.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * collection of static helper methods for Hybrid Temporal Patterns
 */
public final class HTPUtils {

    private HTPUtils() {}

    public static int getLastIndexOfLastStableGroup(HybridTemporalPattern pattern) {

        List<EventNode> nodes = pattern.getEventNodes();
        List<OrderRelation> relations = pattern.getOrderRelations();

        int i = getLastStart(nodes, new ArrayList<>());
        while (i > 0) {
            i--;
            final OrderRelation currentRelation = relations.get(i);

            if (currentRelation == OrderRelation.SMALLER) {
                break;
            }
        }
        return i;
    }

    public static int getLastIndexOfUnmodifiablePart(List<EventNode> nodes, List<IntervalEndEventNode> openEnds) {
        int secondLastStart = -1;

        int i = getLastStart(nodes, openEnds);
        while (i > 0) {
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

                secondLastStart = i;
                break;
            }
        }
        return secondLastStart;
    }

    public static int getLastStart(List<EventNode> nodes, List<IntervalEndEventNode> openEnds) {
        int lastStart = -1;

        int i = nodes.size();
        while (i > 0) {
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

                lastStart = i;
                break;
            }
        }
        return lastStart;
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
        return htp1 == htp2
                || (
                htp1 != null
                        && htp2 != null
                        && htp1.getPatternItemsInIntegerIdOrder().equals(htp2.getPatternItemsInIntegerIdOrder()));
    }

    public static int hashCode(HybridTemporalPattern pattern) {
        return new HashCodeBuilder(13, 31)
                .append(pattern.getPatternItemsInIntegerIdOrder())
                .hashCode();
    }

    /**
     * This method compares two occurrence points. The comparison is made according to the
     * definition 6 (Arrangement of event nodes in htp). in the paper.
     *
     * @param n1  - The first EventNode
     * @param o1 - The occurence point of the first EventNode.
     * @param n2  - The second EventNode
     * @param o2 - The occurence point of the second EventNode.
     * @param useIntId - Whether the comparison should be done by integer id (faster) or string id.
     *                 Resulting order may be different.
     * @return <0 If a is lexically before b, >0 if b is lexically before a, 0 if lexically equal.
     */
    public static int compareOccurrencePoints(HybridEvent o1, EventNode n1, HybridEvent o2, EventNode n2, boolean useIntId) {
        int timeComparison = compareOccurrencePointTimes(o1, n1, o2, n2);
        if (timeComparison != 0) {
            return timeComparison;
        }

        return useIntId ? EventNode.compareByIntId(n1, n2) : EventNode.compareByStringId(n1, n2);
    }

    /**
     * This method compares the times of occurrence of two event nodes.
     *
     * @param n1  - The first EventNode
     * @param o1 - The occurence point of the first EventNode.
     * @param n2  - The second EventNode
     * @param o2 - The occurence point of the second EventNode.
     *
     * @return <0 If a is temporally before b, >0 if b is temporally before a, 0 if equal times.
     */
    public static int compareOccurrencePointTimes(HybridEvent o1, EventNode n1, HybridEvent o2, EventNode n2) {
        double time1 = n1 instanceof PointEventNode ? o1.getStartPoint()
                : (n1 instanceof IntervalStartEventNode ? o1.getStartPoint() : o1.getEndPoint());
        double time2 = n2 instanceof PointEventNode ? o2.getStartPoint()
                : (n2 instanceof IntervalStartEventNode ? o2.getStartPoint() : o2.getEndPoint());

        return Double.compare(time1, time2);
    }
}
