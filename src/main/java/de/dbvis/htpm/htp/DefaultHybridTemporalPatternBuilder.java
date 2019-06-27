package de.dbvis.htpm.htp;

import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.eventnodes.*;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.*;

/**
 * A helper class that appends EventNodes and their occurrences in order to build a pattern.
 *
 * @author Wolfgang Jentner
 *
 */
public class DefaultHybridTemporalPatternBuilder {

    protected final List<EventNode> ev;
    protected final List<OrderRelation> ors;
    protected final List<HybridEvent> ops;
    protected final HybridEventSequence seq;

    protected final Map<Integer, Integer> occurrencemarks;
    protected final Map<Integer, Map<Integer, Map<Integer, Integer>>> occurrencemarkOfStartinterval;

    private HybridTemporalPattern patternPrefix;
    private Occurrence occurrencePrefix;
    private DefaultHybridTemporalPattern htp;
    private DefaultOccurrence occ;

    public static DefaultHybridTemporalPatternBuilder buildFromHybridEventList(HybridEventSequence seq,
                                                                               List<HybridEvent> events) {
        final int length = events.size();

        DefaultHybridTemporalPatternBuilder builder = new DefaultHybridTemporalPatternBuilder(seq, length);
        int occurrenceMark = 0;

        class NodeOccurrencePointPair implements Comparable<NodeOccurrencePointPair> {
            EventNode eventNode;
            HybridEvent op;

            @Override
            public int compareTo(NodeOccurrencePointPair o) {
                return HTPUtils.compareOccurrencePoints(this.op, this.eventNode, o.op, o.eventNode, true);
            }
        }

        List<NodeOccurrencePointPair> pairs = new ArrayList<>(length * 2);

        for (HybridEvent ev : events) {
            if (ev.isPointEvent()) {
                NodeOccurrencePointPair pair = new NodeOccurrencePointPair();
                pair.eventNode = new PointEventNode(ev.getEventId());
                pair.op = ev;
                pairs.add(pair);
            } else {
                NodeOccurrencePointPair pair1 = new NodeOccurrencePointPair();
                pair1.eventNode = new IntervalStartEventNode(ev.getEventId(), occurrenceMark);
                pair1.op = ev;
                pairs.add(pair1);
                NodeOccurrencePointPair pair2 = new NodeOccurrencePointPair();
                pair2.eventNode = new IntervalEndEventNode(ev.getEventId(), occurrenceMark);
                pair2.op = ev;
                pairs.add(pair2);
                occurrenceMark++;
            }
        }

        pairs.sort(NodeOccurrencePointPair::compareTo);

        for (NodeOccurrencePointPair pair: pairs) {
            builder.append(0, pair.eventNode, pair.op);
        }

        return builder;
    }

    public static DefaultHybridTemporalPatternBuilder buildFromSequence(HybridEventSequence seq) {
        final List<HybridEvent> events = seq.getEvents();

        return buildFromHybridEventList(seq, events);
    }

    public static HybridTemporalPattern buildSubPatternByDeletingNode(HybridTemporalPattern pattern, int deletionIndex) {
        List<EventNode> eventNodes = pattern.getEventNodes();
        EventNode nodeToDelete = eventNodes.get(deletionIndex);

        if (nodeToDelete instanceof IntervalEndEventNode) {
            //go back to find start event node
            for (int i = deletionIndex; i > 0; i--) {
                final EventNode n = eventNodes.get(i);
                if (n instanceof IntervalStartEventNode
                        && ((IntervalStartEventNode) n).getOccurrenceMark() == ((IntervalEndEventNode) nodeToDelete).getOccurrenceMark()) {
                    nodeToDelete = n;
                    deletionIndex = i;
                    break;
                }
            }
        }

        int eventNodeType = nodeToDelete.getIntegerEventID();
        int occMark = -1;
        if (nodeToDelete instanceof IntervalStartEventNode) {
            occMark = ((IntervalStartEventNode) nodeToDelete).getOccurrenceMark();
        }

        final ArrayList<EventNode> subPatternNodes = new ArrayList<>(eventNodes.subList(0, deletionIndex));
        final ArrayList<OrderRelation> subPatternRelations = deletionIndex == 0
                ? new ArrayList<>()
                : new ArrayList<>(pattern.getOrderRelations().subList(0, deletionIndex-1));

        boolean skippedNode = true;
        boolean removeFirstRelation = false;

        for (int j = deletionIndex+1; j < eventNodes.size(); j++) {
            final EventNode nodeToAdd = eventNodes.get(j);
            final OrderRelation relationToAdd;
            if (skippedNode) {
                if (j-2 >= 0) {
                    relationToAdd = pattern.small(j - 2, j);
                } else {
                    //add dummy relation which we later remove
                    relationToAdd = OrderRelation.SMALLER;
                    removeFirstRelation = true;
                }
                skippedNode = false;
            } else {
                relationToAdd = pattern.small(j-1, j);
            }

            if (nodeToAdd instanceof IntervalEventNode && nodeToAdd.getIntegerEventID() == eventNodeType) {
                //is either end event node or other node whose occurrence mark maybe needs to be shifted
                if (((IntervalEventNode) nodeToAdd).getOccurrenceMark() == occMark) {
                    //do not add node, it is the end node of removed node.
                    //We have to get the order relations right! Set flag to do it in next loop.
                    skippedNode = true;
                } else if (((IntervalEventNode) nodeToAdd).getOccurrenceMark() > occMark) {
                    //shift occurrence mark by 1
                    //because this node is an interval of same type with later occurrence than deleted one
                    final int newOccurrenceMark = ((IntervalEventNode) nodeToAdd).getOccurrenceMark() - 1;
                    subPatternNodes.add(nodeToAdd instanceof IntervalStartEventNode
                            ? new IntervalStartEventNode(nodeToAdd, newOccurrenceMark)
                            : new IntervalEndEventNode(nodeToAdd, newOccurrenceMark));
                    subPatternRelations.add(relationToAdd);
                } else {
                    //is Intervaleventnode without occurrence mark shift needed, can be added as-is
                    subPatternNodes.add(nodeToAdd);
                    subPatternRelations.add(relationToAdd);
                }
            } else {
                //this node is not deleted, is PointEventNode
                subPatternNodes.add(nodeToAdd);
                subPatternRelations.add(relationToAdd);
            }
        }

        if (removeFirstRelation) {
            //we put a dummy relation at the beginning, now remove it
            subPatternRelations.remove(0);
        }

        return new DefaultHybridTemporalPattern(subPatternNodes, subPatternRelations);
    }

    //================================================================================
    // Pattern builder to build pattern and occurrence
    // by adding together events from multiple patterns
    //================================================================================

    public DefaultHybridTemporalPatternBuilder(HybridEventSequence seq, int length) {
        this.ev = new ArrayList<>(length * 2);
        this.ors = new ArrayList<>(length * 2);
        this.occurrencemarks = new HashMap<>(2);
        this.occurrencemarkOfStartinterval = new HashMap<>(length);
        this.ops = new ArrayList<>(length * 2);
        this.seq = seq;
    }

    /**
     * This method appends an EventNode to a list. It takes care of start and end nodes.
     * @param frompattern - for occurrence mark mapping
     * @param e - The EventNode to add.
     * @param op - The occurence point of the event node.
     */
    public void append(int frompattern, EventNode e, HybridEvent op) {

        final int eventNodeId = e.getIntegerEventID();

        final EventNode node;

        int offset = 0;

        if(e instanceof PointEventNode) {
            node = new PointEventNode(e);

        } else if(e instanceof IntervalStartEventNode) {
            final IntervalStartEventNode is = (IntervalStartEventNode) e;

            int newOccurrenceMark = this.occurrencemarks.getOrDefault(eventNodeId, -1) + 1;
            this.occurrencemarks.put(eventNodeId, newOccurrenceMark); //update

            final int originalOccurrenceMark = is.getOccurrenceMark();

            occurrencemarkOfStartinterval
                    .computeIfAbsent(frompattern, i -> new HashMap<>())
                    .computeIfAbsent(eventNodeId, i -> new HashMap<>())
                    .put(originalOccurrenceMark, newOccurrenceMark);

            node = new IntervalStartEventNode(e, newOccurrenceMark);

        } else if(e instanceof IntervalEndEventNode) {
            IntervalEndEventNode ie = (IntervalEndEventNode) e;

            final int originalOccurrenceMark = ie.getOccurrenceMark();

            int startNodeOccurrenceMark;
            try {
                startNodeOccurrenceMark = occurrencemarkOfStartinterval
                        .get(frompattern)
                        .get(eventNodeId)
                        .get(originalOccurrenceMark);

            } catch (NullPointerException npe) {
                throw new RuntimeException("Could not find corresponding IntervalStartEventNode for key "
                        + frompattern + " - " + eventNodeId + " - " + originalOccurrenceMark,
                        npe);
            }

            node = new IntervalEndEventNode(e, startNodeOccurrenceMark);

            //end event node order not guaranteed if occurrence marks change
            while (offset < ops.size()
                    && ev.get(ev.size() - 1 - offset) instanceof IntervalEndEventNode
                    && Objects.equals(op.getEndPoint(), ops.get(ops.size() - 1 - offset).getEndPoint())
                    && node.compareTo(ev.get(ev.size() - 1 - offset)) < 1) {
                offset++;
            }

        } else {
            throw new UnsupportedOperationException("Unknown EventNode type");
        }

        if (ev.size() > 0) {
            final int order = HTPUtils.compareOccurrencePointTimes(ops.get(ops.size() - 1), ev.get(ev.size() - 1), op, e);
            if (order < 0) {
                ors.add(OrderRelation.SMALLER);
            } else if (order == 0) {
                ors.add(OrderRelation.EQUAL);
            } else {
                throw new IllegalArgumentException("append OccurrencePoints in order!");
            }
        }

        ev.add(ev.size() - offset, node);
        ops.add(ops.size() - offset, op);
    }

    public void setPrefixes(HybridTemporalPattern patternPrefix, Occurrence occurrencePrefix) {
        this.patternPrefix = patternPrefix;
        this.occurrencePrefix = occurrencePrefix;
    }

    public HybridTemporalPattern getPatternPrefix() {
        return patternPrefix;
    }

    public Occurrence getOccurrencePrefix() {
        return occurrencePrefix;
    }

    public HybridTemporalPattern getPattern() {
        if (htp == null) {
            htp = new DefaultHybridTemporalPattern(ev, ors);
        }
        return htp;
    }

    public Occurrence getOccurence() {
        if (this.occ == null) {
            occ = new DefaultOccurrence(seq, ops);
        }
        return this.occ;
    }
}
