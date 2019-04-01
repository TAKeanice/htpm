package de.dbvis.htpm.htp;

import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.eventnodes.*;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.DefaultOccurrencePoint;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.occurrence.OccurrencePoint;

import java.util.*;

/**
 * A helper class that appends EventNodes and their occurrences in order to build a pattern.
 *
 * @author Wolfgang Jentner
 *
 */
public class DefaultHybridTemporalPatternBuilder {

    public static DefaultHybridTemporalPatternBuilder buildFromHybridEventList(HybridEventSequence seq,
                                                                               List<HybridEvent> events) {
        final int length = events.size();

        DefaultHybridTemporalPatternBuilder builder = new DefaultHybridTemporalPatternBuilder(seq, length);
        int occurrenceMark = 0;

        class NodeOccurrencePointPair implements Comparable<NodeOccurrencePointPair> {
            EventNode eventNode;
            OccurrencePoint op;

            @Override
            public int compareTo(NodeOccurrencePointPair o) {
                int opComp = op.compareTo(o.op);
                if (opComp != 0) {
                    return opComp;
                }
                return eventNode.compareTo(o.eventNode);
            }
        }

        List<NodeOccurrencePointPair> pairs = new ArrayList<>(length * 2);

        for (HybridEvent ev : events) {
            if (ev.isPointEvent()) {
                NodeOccurrencePointPair pair = new NodeOccurrencePointPair();
                pair.eventNode = new PointEventNode(ev.getEventId());
                pair.op =new DefaultOccurrencePoint(ev);
                pairs.add(pair);
            } else {
                NodeOccurrencePointPair pair1 = new NodeOccurrencePointPair();
                pair1.eventNode = new IntervalStartEventNode(ev.getEventId(), occurrenceMark);
                pair1.op =new DefaultOccurrencePoint(ev, true);
                pairs.add(pair1);
                NodeOccurrencePointPair pair2 = new NodeOccurrencePointPair();
                pair2.eventNode = new IntervalEndEventNode(ev.getEventId(), occurrenceMark);
                pair2.op =new DefaultOccurrencePoint(ev, false);
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

    protected final List<EventNode> ev;
    protected final List<OrderRelation> ors;
    protected final List<OccurrencePoint> ops;
    protected final HybridEventSequence seq;

    protected final Map<Integer, Integer> occurrencemarks;
    protected final Map<Integer, Map<Integer, Map<Integer, Integer>>> occurrencemarkOfStartinterval;

    private HybridTemporalPattern patternPrefix;
    private Occurrence occurrencePrefix;
    private DefaultHybridTemporalPattern htp;
    private DefaultOccurrence occ;

    public static DefaultHybridTemporalPatternBuilder buildFromSequence(HybridEventSequence seq) {
        final List<HybridEvent> events = seq.getEvents();

        return buildFromHybridEventList(seq, events);
    }

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
    public void append(int frompattern, EventNode e, OccurrencePoint op) {

        final int eventNodeId = e.getIntegerEventID();

        final EventNode node;

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

            int newOccurrenceMark;
            try {
                newOccurrenceMark = occurrencemarkOfStartinterval
                        .get(frompattern)
                        .get(eventNodeId)
                        .get(originalOccurrenceMark);

            } catch (NullPointerException npe) {
                throw new RuntimeException("Could not find corresponding IntervalStartEventNode for key "
                        + frompattern + " - " + eventNodeId + " - " + originalOccurrenceMark,
                        npe);
            }

            node = new IntervalEndEventNode(e, newOccurrenceMark);

        } else {
            throw new UnsupportedOperationException("Unknown EventNode type");
        }

        if (ev.size() > 0) {
            final int order = ops.get(ops.size() - 1).compareTo(op);
            if (order < 0) {
                ors.add(OrderRelation.SMALLER);
            } else if (order == 0) {
                ors.add(OrderRelation.EQUAL);
            } else {
                throw new IllegalArgumentException("append OccurrencePoints in order!");
            }
        }

        ev.add(node);
        ops.add(op);
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
