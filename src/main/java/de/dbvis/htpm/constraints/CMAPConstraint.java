package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.*;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.*;

import static de.dbvis.htpm.htp.eventnodes.OrderRelation.EQUAL;
import static de.dbvis.htpm.htp.eventnodes.OrderRelation.SMALLER;

/**
 * Only applicable on BFS or when ALL two-patterns are built BEFORE switching to DFS
 * Has to be paired with a DefaultHTPMConstraint or some derived constraint
 */
public class CMAPConstraint extends AcceptAllConstraint {

    private final Set<HybridTemporalPattern> twoPatterns = new HashSet<>();

    private int joinPreventedCount = 0;

    @Override
    public boolean patternsQualifyForJoin(HybridTemporalPattern commonPrefix, HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k) {
        if (k <= 2) {
            return true;
        }

        //check new two-pattern suffixes which can be made by joining the suffix of first and second pattern

        List<EventNode> pa1 = firstPattern.getEventNodes();
        List<EventNode> pa2 = secondPattern.getEventNodes();
        List<EventNode> pre;

        List<OrderRelation> relations1 = firstPattern.getOrderRelations();
        List<OrderRelation> relations2 = secondPattern.getOrderRelations();

        if (commonPrefix != null) {
            pre = commonPrefix.getEventNodes();
        } else {
            pre = Collections.emptyList();
        }

        IndexPair index1 = determineGroupIndices(pa1, pre, relations1);
        IndexPair index2 = determineGroupIndices(pa2, pre, relations2);

        final boolean accept = testPatterns(pa1, pa2, index1, index2);
        if (!accept) {
            joinPreventedCount++;
        }
        return accept;
    }

    @Override
    public boolean shouldOutputOccurrence(HybridTemporalPattern p, Occurrence occurrence) {
        return true;
    }

    @Override
    public boolean shouldOutputPattern(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return true;
    }

    @Override
    public void foundPattern(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        if (k == 2) {
            //save two-patterns to CMAP index
            twoPatterns.add(p);
        }
    }

    @Override
    public int getPatternJoinPreventedCount() {
        return joinPreventedCount;
    }

    @Override
    public int getOccurrenceJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return 0;
    }

    @Override
    public int getPatternsDiscardedCount() {
        return 0;
    }

    @Override
    public int getBranchesCutCount() {
        return 0;
    }

    private boolean testPatterns(List<EventNode> pa1, List<EventNode> pa2,
                                 IndexPair index1, IndexPair index2) {

        //build possible 2-patterns and apply constraint

        final EventNode firstStart = pa1.get(index1.startIndex);
        final EventNode secondStart = pa2.get(index2.startIndex);

        final boolean firstIsInterval = index1.endGroup >= 0;
        final boolean secondIsInterval = index2.endGroup >= 0;

        final EventNode firstEnd = firstIsInterval ? pa1.get(index1.endIndex) : null;
        final EventNode secondEnd = secondIsInterval ? pa2.get(index2.endIndex) : null;

        final boolean firstAndSecondSameIntervalType = firstIsInterval && secondIsInterval
                && firstStart.getIntegerEventID() == secondStart.getIntegerEventID();
        int firstOccurrenceMark = 0;
        int secondOccurrenceMark = firstAndSecondSameIntervalType ? 1 : 0;

        if (index1.startGroup < index2.startGroup) {
            //FIRST START FIRST
            return testPatternsStartBeforeStart(index1, index2,
                    firstStart, secondStart, firstEnd, secondEnd, false,
                    firstOccurrenceMark, secondOccurrenceMark);
        } else if (index1.startGroup > index2.startGroup) {
            //SECOND START FIRST
            return testPatternsStartBeforeStart(index2, index1,
                    secondStart, firstStart, secondEnd, firstEnd, true,
                    firstOccurrenceMark, secondOccurrenceMark);
        } else if (index1.startGroup % 2 == 1) {
            //both starts are fixed at the same time as some prefix node
            //FIRST START == SECOND START
            return testPatternsStartEqualsStart(index1, index2,
                    firstStart, secondStart, firstEnd, secondEnd,
                    firstOccurrenceMark, secondOccurrenceMark);
        } else {
            //Multiple possible arrangements for start event nodes
            //FIRST START ?? SECOND START

            //test multiple variants
            boolean firstStartFirst = //FIRST START FIRST
                    testPatternsStartBeforeStart(index1, index2,
                            firstStart, secondStart, firstEnd, secondEnd, false,
                            firstOccurrenceMark, secondOccurrenceMark);
            if (firstStartFirst) {
                return true;
            }

            boolean secondStartFirst = //SECOND START FIRST
                    testPatternsStartBeforeStart(index2, index1,
                            secondStart, firstStart, secondEnd, firstEnd, true,
                            firstOccurrenceMark, secondOccurrenceMark);
            if (secondStartFirst) {
                return true;
            }

            //FIRST START == SECOND START
            return testPatternsStartEqualsStart(index1, index2,
                    firstStart, secondStart, firstEnd, secondEnd,
                    firstOccurrenceMark, secondOccurrenceMark);
        }
    }

    private boolean testPatternsStartBeforeStart(IndexPair index1, IndexPair index2,
                                                 EventNode firstStart, EventNode secondStart, EventNode firstEnd, EventNode secondEnd,
                                                 boolean inverted,
                                                 int firstOccurrenceMark, int secondOccurrenceMark) {

        ArrayList<EventNode> nodes = new ArrayList<>(4);
        ArrayList<OrderRelation> relations = new ArrayList<>(3);

        final boolean firstIsInterval = index1.endGroup >= 0;
        final boolean secondIsInterval = index2.endGroup >= 0;

        //FIRST START FIRST
        nodes.add(firstIsInterval ? new IntervalStartEventNode(firstStart, firstOccurrenceMark)
                : new PointEventNode(firstStart));

        if ((!firstIsInterval) || index2.startGroup < index1.endGroup) {
            //SECOND START SECOND
            return testPatternsSecondStartSecond(index1, index2,
                    secondStart, firstEnd, secondEnd, inverted,
                    firstOccurrenceMark, secondOccurrenceMark,
                    secondIsInterval,
                    nodes, relations);
        } else if (index2.startGroup > index1.endGroup) {
            //FIRST END SECOND
            return testPatternsFirstEndSecond(secondStart, firstEnd, secondEnd,
                    firstOccurrenceMark, secondOccurrenceMark,
                    secondIsInterval,
                    nodes, relations);
        } else if (index2.startGroup % 2 == 1) {
            //both are fixed at the same time as some prefix node
            //SECOND START == FIRST END
            return testPatternsStartEqualsEnd(secondStart, firstEnd, secondEnd,
                    firstOccurrenceMark, secondOccurrenceMark,
                    secondIsInterval, nodes, relations);
        } else {
            //relation of first start and second end unknown
            //FIRST START ?? SECOND END
            //test multiple variants

            ArrayList<EventNode> nodes2 = new ArrayList<>(nodes);
            ArrayList<OrderRelation> relations2 = new ArrayList<>(relations);

            //SECOND START SECOND
            boolean secondStartSecond = testPatternsSecondStartSecond(index1, index2, secondStart, firstEnd, secondEnd, inverted, firstOccurrenceMark, secondOccurrenceMark, secondIsInterval, nodes2, relations2);
            if (secondStartSecond) {
                return true;
            }

            ArrayList<EventNode> nodes3 = new ArrayList<>(nodes);
            ArrayList<OrderRelation> relations3 = new ArrayList<>(relations);

            //FIRST END SECOND
            boolean firstEndSecond = testPatternsFirstEndSecond(secondStart, firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, secondIsInterval, nodes3, relations3);
            if (firstEndSecond) {
                return true;
            }

            //SECOND START == FIRST END
            return testPatternsStartEqualsEnd(secondStart, firstEnd, secondEnd,
                    firstOccurrenceMark, secondOccurrenceMark,
                    secondIsInterval, nodes, relations);
        }
    }

    private boolean testPatternsStartEqualsStart(IndexPair index1, IndexPair index2,
                                                 EventNode firstStart, EventNode secondStart, EventNode firstEnd, EventNode secondEnd,
                                                 int firstOccurrenceMark, int secondOccurrenceMark) {

        ArrayList<EventNode> nodes = new ArrayList<>(4);
        ArrayList<OrderRelation> relations = new ArrayList<>(3);

        final boolean firstIsInterval = index1.endGroup >= 0;
        final boolean secondIsInterval = index2.endGroup >= 0;

        if (firstStart.compareTo(secondStart) < 0) {
            nodes.add(firstIsInterval ? new IntervalStartEventNode(firstStart, firstOccurrenceMark)
                    : new PointEventNode(firstStart));
            nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, secondOccurrenceMark)
                    : new PointEventNode(secondStart));
            relations.add(EQUAL);
            return testPatternsEndVsEnd(index1, index2, firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, nodes, relations);
        } else {
            nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, firstOccurrenceMark)
                    : new PointEventNode(secondStart));
            nodes.add(firstIsInterval ? new IntervalStartEventNode(firstStart, secondOccurrenceMark)
                    : new PointEventNode(firstStart));
            relations.add(EQUAL);
            return testPatternsEndVsEnd(index1, index2, firstEnd, secondEnd, secondOccurrenceMark, firstOccurrenceMark, nodes, relations);
        }
    }

    private boolean testPatternsFirstEndSecond(EventNode secondStart, EventNode firstEnd, EventNode secondEnd,
                                               int firstOccurrenceMark, int secondOccurrenceMark,
                                               boolean secondIsInterval,
                                               ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {
        nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
        relations.add(SMALLER);
        //SECOND START THIRD
        nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, secondOccurrenceMark)
                : new PointEventNode(secondStart));
        relations.add(SMALLER);
        return testPatternsLastEnd(secondEnd, secondOccurrenceMark, secondIsInterval, nodes, relations);
    }

    private boolean testPatternsSecondStartSecond(IndexPair index1, IndexPair index2, EventNode secondStart, EventNode firstEnd, EventNode secondEnd, boolean inverted, int firstOccurrenceMark, int secondOccurrenceMark, boolean secondIsInterval, ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {
        boolean secondStartSecond; //SECOND START SECOND
        nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, secondOccurrenceMark)
                : new PointEventNode(secondStart));
        relations.add(SMALLER);
        if (inverted) {
            secondStartSecond = testPatternsEndVsEnd(index2, index1, secondEnd, firstEnd, secondOccurrenceMark, firstOccurrenceMark, nodes, relations);
        } else {
            secondStartSecond = testPatternsEndVsEnd(index1, index2, firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, nodes, relations);
        }
        return secondStartSecond;
    }

    private boolean testPatternsEndVsEnd(IndexPair index1, IndexPair index2,
                                         EventNode firstEnd, EventNode secondEnd,
                                         int firstOccurrenceMark, int secondOccurrenceMark,
                                         ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {

        final boolean firstIsInterval = index1.endGroup >= 0;
        final boolean secondIsInterval = index2.endGroup >= 0;

        if (!secondIsInterval) {
            //end node of second pattern suffix nonexistent
            //FIRST END THIRD
            return testPatternsLastEnd(firstEnd, firstOccurrenceMark, firstIsInterval, nodes, relations);
        } else if (!firstIsInterval) {
            //end node of first pattern suffix nonexistent
            //SECOND END THIRD
            return testPatternsLastEnd(secondEnd, secondOccurrenceMark, secondIsInterval, nodes, relations);
        } else if (index1.endGroup < index2.endGroup) {
            return testPatternsFirstEndThird(firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, secondIsInterval, nodes, relations);
        } else if (index1.endGroup > index2.endGroup) {
            //SECOND END THIRD
            return testPatternsFirstEndThird(secondEnd, firstEnd, secondOccurrenceMark, firstOccurrenceMark, firstIsInterval, nodes, relations);
        } else if (index1.endGroup % 2 == 1) {
            //both end nodes are fixed at the same time as some prefix node
            //FIRST END == SECOND END
            return testPatternsEndEqualsEnd(firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, nodes, relations);
        } else {
            //unclear order of end event nodes
            //FIRST END ?? SECOND END
            //test multiple variants

            ArrayList<EventNode> nodes2 = new ArrayList<>(nodes);
            ArrayList<OrderRelation> relations2 = new ArrayList<>(relations);
            //FIRST END THIRD
            boolean firstEndThird = testPatternsFirstEndThird(firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, secondIsInterval, nodes2, relations2);
            if (firstEndThird) {
                return true;
            }

            ArrayList<EventNode> nodes3 = new ArrayList<>(nodes);
            ArrayList<OrderRelation> relations3 = new ArrayList<>(relations);
            //SECOND END THIRD
            boolean secondEndThird = testPatternsFirstEndThird(secondEnd, firstEnd, secondOccurrenceMark, firstOccurrenceMark, firstIsInterval, nodes3, relations3);
            if (secondEndThird) {
                return true;
            }

            //FIRST END == SECOND END
            return testPatternsEndEqualsEnd(firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, nodes, relations);
        }
    }

    private boolean testPatternsFirstEndThird(EventNode firstEnd, EventNode secondEnd,
                                              int firstOccurrenceMark, int secondOccurrenceMark,
                                              boolean secondIsInterval,
                                              ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {
        //FIRST END THIRD
        nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
        relations.add(SMALLER);
        //SECOND END FOURTH
        return testPatternsLastEnd(secondEnd, secondOccurrenceMark, secondIsInterval, nodes, relations);
    }


    private boolean testPatternsStartEqualsEnd(EventNode secondStart, EventNode firstEnd, EventNode secondEnd,
                                               int firstOccurrenceMark, int secondOccurrenceMark,
                                               boolean secondIsInterval,
                                               ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {

        //FIRST END EQUALS SECOND START
        if (firstEnd.compareTo(secondStart) < 0) {
            nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
            nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, secondOccurrenceMark)
                    : new PointEventNode(secondStart));
        } else {
            nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, secondOccurrenceMark)
                    : new PointEventNode(secondStart));
            nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
        }
        relations.add(SMALLER);
        relations.add(EQUAL);

        return testPatternsLastEnd(secondEnd, secondOccurrenceMark, secondIsInterval, nodes, relations);
    }

    private boolean testPatternsEndEqualsEnd(EventNode firstEnd, EventNode secondEnd,
                                             int firstOccurrenceMark, int secondOccurrenceMark,
                                             ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {
        //FIRST END == SECOND END
        if (firstEnd.compareTo(secondEnd) < 0) {
            nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
            nodes.add(new IntervalEndEventNode(secondEnd, secondOccurrenceMark));
        } else {
            nodes.add(new IntervalEndEventNode(secondEnd, secondOccurrenceMark));
            nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
        }
        relations.add(SMALLER);
        relations.add(EQUAL);
        //test
        return testCompletePattern(nodes, relations);
    }

    private boolean testPatternsLastEnd(EventNode end, int occurrenceMark, boolean isInterval,
                                        ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {
        if (isInterval) {
            //END LAST
            nodes.add(new IntervalEndEventNode(end, occurrenceMark));
            relations.add(SMALLER);
        }
        //test
        return testCompletePattern(nodes, relations);
    }


    private boolean testCompletePattern(List<EventNode> nodes, List<OrderRelation> relations) {
        return twoPatterns.contains(new DefaultHybridTemporalPattern(nodes, relations));
    }


    /**
     * Group indices of start and end node:
     * Even number means the node is between prefix nodes, odd number that it is at the same time as a prefix node
     * The group index helps us decide where the node goes in a two-pattern:
     * If two nodes have different group indices, they will be ordered by their group index order in the joined pattern.
     * If two nodes have the same _odd_ group index, their time order in the joined pattern will be equal
     * if two nodes have the same even group index, their time order in the joined pattern is unedecided.
     *
     * @param pa pattern nodes
     * @param pre prefix nodes
     * @param relations relations of nodes in pattern
     * @return the groups to which the suffix nodes in the pattern belong
     */
    public IndexPair determineGroupIndices(List<EventNode> pa, List<EventNode> pre, List<OrderRelation> relations) {

        int startGroup = -1;
        int startIndex = -1;
        int endGroup = -1;
        int endIndex = -1;

        int preIndex = 0;
        int groupNumber = 1;
        int i = 0;
        while (i < pa.size()) {
            final EventNode paNode = pa.get(i);
            final EventNode preNode = pre.size() > preIndex ? pre.get(preIndex) : null;
            if (!paNode.equals(preNode)) {
                //we are looking at a node that was inserted into the prefix in the last join
                if (paNode instanceof IntervalEndEventNode) {
                    endGroup = determineGroupIndexOfNode(i, groupNumber, relations);
                    endIndex = i;
                    //we are done, have found start and end node of interval that is the pattern suffix
                    break;
                } else {
                    startGroup = determineGroupIndexOfNode(i, groupNumber, relations);
                    startIndex = i;
                    if (paNode instanceof PointEventNode) {
                        //we are done, have found the point node that is the suffix of this pattern
                        break;
                    }
                    if (0 < i && relations.get(i-1) != SMALLER && relations.get(i) == SMALLER) {
                        //we have a new (prefix) group afterwards
                        groupNumber += 2;
                    }
                }
                i++;
            } else {
                //we are looking at a node from the prefix
                preIndex++;
                if (i < relations.size() && relations.get(i) == SMALLER) {
                    //next relation is a SMALLER relation -> next prefix node is in next group
                    // if there is no prefix node any more, upcoming nodes are "after last prefix group"
                    groupNumber += 2;
                }
                i++;
            }
        }
        return new IndexPair(startGroup, endGroup, startIndex, endIndex);
    }

    public int determineGroupIndexOfNode(int nodeNumber, int currentGroupIndex, List<OrderRelation> relations) {
        int index;
        if (0 < nodeNumber && relations.get(nodeNumber - 1) != SMALLER) {
            //we are at same position as current group
            index = currentGroupIndex;
        } else if (nodeNumber == relations.size()
                || nodeNumber < relations.size() && relations.get(nodeNumber) == SMALLER) {
            //we have already changed groups, but did not land in group from prefix (or behind last prefix node)
            // -> next (prefix) node will be after this node (or does not exist)
            // -> this node is between last and next prefix group (or after last prefix group)
            index = currentGroupIndex - 1;
        } else {
            // we are at same position as current group
            // or in next group, and current group index already set to next prefix group
            index = currentGroupIndex;
        }
        return index;
    }

    static class IndexPair {
        final int startGroup;
        final int endGroup;
        final int startIndex;
        final int endIndex;

        IndexPair(int startGroup, int endGroup, int startIndex, int endIndex) {
            this.startGroup = startGroup;
            this.endGroup = endGroup;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }

    @Override
    public String toString() {
        return "CMAP Constraint (prevented" + joinPreventedCount + "joins )";
    }
}
