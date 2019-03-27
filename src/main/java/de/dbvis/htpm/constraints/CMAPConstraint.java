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

    private final SupportCounter supportCounter;
    private final Set<HybridTemporalPattern> twoPatterns = new HashSet<>();

    CMAPConstraint(DefaultHTPMConstraint supportCounter) {

        this.supportCounter = supportCounter;
    }

    @Override
    public boolean patternsQualifyForJoin(HybridTemporalPattern firstPattern, HybridTemporalPattern secondPattern, int k) {
        if (k <= 2) {
            return true;
        }

        //check new two-pattern suffixes which can be made by joining the suffix of first and second pattern

        HybridTemporalPattern prefix = firstPattern.getPrefix();

        List<EventNode> pa1 = firstPattern.getEventNodes();
        List<EventNode> pa2 = secondPattern.getEventNodes();
        List<EventNode> pre;

        List<OrderRelation> relations1 = firstPattern.getOrderRelations();
        List<OrderRelation> relations2 = secondPattern.getOrderRelations();

        if (prefix != null) {
            pre = prefix.getEventNodes();
        } else {
            pre = Collections.emptyList();
        }

        IndexPair index1 = determineGroupIndices(pa1, pre, relations1);
        IndexPair index2 = determineGroupIndices(pa2, pre, relations2);

        //build possible 2-patterns and apply constraint
        List<List<EventNode>> twoPatternNodes = new ArrayList<>(1);
        List<List<OrderRelation>> twoPatternRelations = new ArrayList<>(1);

        assert index1.startGroup < index1.endGroup || index1.endGroup < 0;
        assert index2.startGroup < index2.endGroup || index2.endGroup < 0;

        ArrayList<EventNode> nodes = new ArrayList<>(4);
        ArrayList<OrderRelation> relations = new ArrayList<>(3);
        twoPatternNodes.add(nodes);
        twoPatternRelations.add(relations);

        return testPotentialPatterns(pa1, pa2, index1, index2, nodes, relations);
    }

    private boolean testPotentialPatterns(List<EventNode> pa1, List<EventNode> pa2,
                                          IndexPair index1, IndexPair index2,
                                          ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {

        final EventNode firstStart = pa1.get(index1.startIndex);
        final EventNode secondStart = pa2.get(index2.startIndex);
        final EventNode firstEnd = pa1.get(index1.endIndex);
        final EventNode secondEnd = pa2.get(index2.endIndex);

        final boolean firstIsInterval = index1.endGroup >= 0;
        final boolean secondIsInterval = index2.endGroup >= 0;

        final boolean firstAndSecondSameIntervalType = firstIsInterval && secondIsInterval
                && firstStart.getIntegerEventID() == secondStart.getIntegerEventID();
        int firstOccurrenceMark = 0;
        int secondOccurrenceMark = firstAndSecondSameIntervalType ? 1 : 0;

        if (index1.startGroup < index2.startGroup) {
            //FIRST START FIRST
            return testPotentialPatternsLevel2(index1, index2,
                    firstStart, secondStart, firstEnd, secondEnd, false,
                    firstOccurrenceMark, secondOccurrenceMark,
                    nodes, relations);
        } else if (index1.startGroup > index2.startGroup) {
            //SECOND START FIRST
            return testPotentialPatternsLevel2(index2, index1,
                    secondStart, firstStart, secondEnd, firstEnd, true,
                    firstOccurrenceMark, secondOccurrenceMark,
                    nodes, relations);
        } else if (index1.startGroup % 2 == 1) {
            //both starts are fixed at the same time as some prefix node
            //FIRST START == SECOND START
            if (firstStart.compareTo(secondStart) < 0) {
                nodes.add(firstIsInterval ? new IntervalStartEventNode(firstStart, firstOccurrenceMark)
                        : new PointEventNode(firstStart));
                nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, secondOccurrenceMark)
                        : new PointEventNode(secondStart));
                relations.add(EQUAL);
                return testPotentialPatternsLevel3(index1, index2, firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, nodes, relations);
            } else {
                nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, firstOccurrenceMark)
                        : new PointEventNode(secondStart));
                nodes.add(firstIsInterval ? new IntervalStartEventNode(firstStart, secondOccurrenceMark)
                        : new PointEventNode(firstStart));
                relations.add(EQUAL);
                return testPotentialPatternsLevel3(index1, index2, firstEnd, secondEnd, secondOccurrenceMark, firstOccurrenceMark, nodes, relations);
            }
        } else {
            //amargeddon for start event nodes
            //FIRST START ?? SECOND START
            //TODO: test multiple variants
            return true;
        }
    }

    private boolean testPotentialPatternsLevel2(IndexPair index1, IndexPair index2,
                                                EventNode firstStart, EventNode secondStart, EventNode firstEnd, EventNode secondEnd,
                                                boolean inverted,
                                                int firstOccurrenceMark, int secondOccurrenceMark,
                                                ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {

        final boolean firstIsInterval = index1.endGroup >= 0;
        final boolean secondIsInterval = index2.endGroup >= 0;

        //FIRST START FIRST
        nodes.add(firstIsInterval ? new IntervalStartEventNode(firstStart, firstOccurrenceMark)
                : new PointEventNode(firstStart));

        if (index2.startGroup < index1.endGroup) {
            //SECOND START SECOND
            nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, secondOccurrenceMark)
                    : new PointEventNode(secondStart));
            relations.add(SMALLER);
            if (inverted) {
                return testPotentialPatternsLevel3(index2, index1, secondEnd, firstEnd, secondOccurrenceMark, firstOccurrenceMark, nodes, relations);
            } else {
                return testPotentialPatternsLevel3(index1, index2, firstEnd, secondEnd, firstOccurrenceMark, secondOccurrenceMark, nodes, relations);
            }
        } else if (index2.startGroup > index1.endGroup) {
            //FIRST END SECOND
            nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
            relations.add(SMALLER);
            //SECOND START THIRD
            nodes.add(secondIsInterval ? new IntervalStartEventNode(secondStart, secondOccurrenceMark)
                    : new PointEventNode(secondStart));
            relations.add(SMALLER);
            if (secondIsInterval) {
                //SECOND END FOURTH
                nodes.add(new IntervalEndEventNode(secondEnd, secondOccurrenceMark));
                relations.add(SMALLER);
            }
            //test
            return testPattern(nodes, relations);
        } else if (index2.startGroup % 2 == 1) {
            //both are fixed at the same time as some prefix node
            //SECOND START == FIRST END
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
            if (secondIsInterval) {
                //SECOND END FOURTH
                nodes.add(new IntervalEndEventNode(secondEnd, secondOccurrenceMark));
                relations.add(SMALLER);
            }
            //test
            return testPattern(nodes, relations);
        } else {
            //amargeddon for first start with second end
            //FIRST START ?? SECOND END
            //TODO: test multiple variants
            return true;
        }
    }

    private boolean testPotentialPatternsLevel3(IndexPair index1, IndexPair index2,
                                                EventNode firstEnd, EventNode secondEnd,
                                                int firstOccurrenceMark, int secondOccurrenceMark,
                                                ArrayList<EventNode> nodes, ArrayList<OrderRelation> relations) {

        final boolean firstIsInterval = index1.endGroup >= 0;
        final boolean secondIsInterval = index2.endGroup >= 0;

        if (!secondIsInterval) {
            //end node of second pattern suffix nonexistent
            if (firstIsInterval) {
                //FIRST END THIRD
                nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
                relations.add(SMALLER);
            }
            //test
            return testPattern(nodes, relations);
        } else {
            if (!firstIsInterval) {
                //end node of first pattern suffix nonexistent
                //SECOND END THIRD
                nodes.add(new IntervalEndEventNode(secondEnd, secondOccurrenceMark));
                relations.add(SMALLER);
                //test
                return testPattern(nodes, relations);
            } else if (index1.endGroup < index2.endGroup) {
                //FIRST END THIRD
                nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
                relations.add(SMALLER);
                //SECOND END FOURTH
                nodes.add(new IntervalEndEventNode(secondEnd, secondOccurrenceMark));
                relations.add(SMALLER);
                //test
                return testPattern(nodes, relations);
            } else if (index1.endGroup > index2.endGroup) {
                //SECOND END THIRD
                nodes.add(new IntervalEndEventNode(secondEnd, secondOccurrenceMark));
                relations.add(SMALLER);
                //FIRST END FOURTH
                nodes.add(new IntervalEndEventNode(firstEnd, firstOccurrenceMark));
                relations.add(SMALLER);
                //test
                return testPattern(nodes, relations);
            } else if (index1.endGroup % 2 == 1) {
                //both end nodes are fixed at the same time as some prefix node
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
                return testPattern(nodes, relations);
            } else {
                //amargeddon, for end event nodes
                //FIRST END ?? SECOND END
                //TODO: test multiple variants
                return true;
            }
        }
    }


    private boolean testPattern(List<EventNode> nodes, List<OrderRelation> relations) {
        return twoPatterns.contains(new DefaultHybridTemporalPattern(nodes, relations, null));
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
                    /* no need to continue group counting any more
                    if (0 < i && relations.get(i-1) != SMALLER && relations.get(i) == SMALLER) {
                        //we have a new (prefix) group afterwards
                        groupNumber += 2;
                    }
                    */
                } else {
                    startGroup = determineGroupIndexOfNode(i, groupNumber, relations);
                    startIndex = i;
                    if (0 < i && relations.get(i-1) != SMALLER && relations.get(i) == SMALLER) {
                        //we have a new (prefix) group afterwards
                        groupNumber += 2;
                    }
                    if (paNode instanceof PointEventNode) {
                        //we are done, have found the point node that is the suffix of this pattern
                        break;
                    }
                }
                i++;
            } else {
                //we are looking at a node from the prefix
                if (i < relations.size() && relations.get(i) == SMALLER) {
                    //next relation is a SMALLER relation -> next prefix node is in next group
                    groupNumber += 2;
                }
                preIndex++;
                i++;
            }
        }
        return new IndexPair(startGroup, endGroup, startIndex, endIndex);
    }

    public int determineGroupIndexOfNode(int nodeNumber, int currentGroupIndex, List<OrderRelation> relations) {
        int startIndex;
        if (0 < nodeNumber && relations.get(nodeNumber - 1) != SMALLER) {
            //we are at same position as current group
            startIndex = currentGroupIndex;
        } else if (nodeNumber < relations.size() && relations.get(nodeNumber) == SMALLER) {
            //we have already changed groups, but did not land in group from prefix
            // -> next (prefix) node will be after this node -> is between last and next group
            startIndex = currentGroupIndex - 1;
        } else {
            // we are at same position as current group
            // or in next group, and current group index already set to next prefix group
            startIndex = currentGroupIndex;
        }
        return startIndex;
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        //we only override this method for its side effect
        if (k == 2 && supportCounter.isSupported(p, occurrences, k)) {
            twoPatterns.add(p);
        }
        return true;
    }

    class IndexPair {
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
}
