package de.dbvis.htpm;

import de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.occurrence.OccurrencePoint;

import java.util.Collections;
import java.util.List;

public class ORAlignment {
    /**
     * This method aligns two pattern according to "Example 7 (Joining two occurrence records)."
     *
     * @param prefix - The shared prefix of the patterns.
     * @param p1     - The first pattern.
     * @param or1    - The occurence points of the first pattern.
     * @param p2     - The second pattern.
     * @param or2    - The occurence points of the second pattern.
     * @param k      - The size of the upcoming pattern
     * @return Returns a map containing one pattern with one SeriesOccurence.
     */
    public static DefaultHybridTemporalPatternBuilder ORAlign(final HybridTemporalPattern prefix,
                                                              final HybridTemporalPattern p1, final Occurrence or1,
                                                              final HybridTemporalPattern p2, final Occurrence or2, int k) {
        int i1 = 0;
        int i2 = 0;
        int ip = 0;

        List<EventNode> pa1 = p1.getEventNodes();
        List<EventNode> pa2 = p2.getEventNodes();

        List<EventNode> pre;

        if (prefix != null) {
            pre = prefix.getEventNodes();
        } else {
            pre = Collections.emptyList();
        }

        DefaultHybridTemporalPatternBuilder b = new DefaultHybridTemporalPatternBuilder(or1.getHybridEventSequence(), k);

        boolean foundPrefix = false;

        while (i1 < pa1.size() && i2 < pa2.size()) {
            final OccurrencePoint op1 = or1.get(i1);
            final OccurrencePoint op2 = or2.get(i2);
            double occurrence1 = op1.getTimePoint();
            double occurrence2 = op2.getTimePoint();
            final EventNode n1 = pa1.get(i1);
            final EventNode n2 = pa2.get(i2);
            final EventNode nP = pre.size() > ip ? pre.get(ip) : null;

            if (n1.equals(nP) && n2.equals(nP)) {
                //both nodes are part of the "prefix", so it does not matter what we append
                b.append(0, n1, op1);
                i1++;
                i2++;
                ip++;
            } else if (compare(n1, occurrence1, n2, occurrence2) < 0) {
                if (!foundPrefix) {
                    b.setPrefixes(p1, or1);
                    foundPrefix = true;
                }
                b.append(0, n1, op1);
                i1++;
            } else {
                if (!foundPrefix) {
                    b.setPrefixes(p2, or2);
                    foundPrefix = true;
                }
                b.append(1, n2, op2);
                i2++;
            }
        }

        if (i1 < pa1.size()) {
            do {
                b.append(0, pa1.get(i1), or1.get(i1));
                i1++;
            } while (i1 < pa1.size());
        } else if (i2 < pa2.size()) {
            do {
                b.append(1, pa2.get(i2), or2.get(i2));
                i2++;
            } while (i2 < pa2.size());
        }
        return b;
    }

    /**
     * This method compares two event nodes. The comparison is made according to the
     * definition 6 (Arrangement of event nodes in htp). in the paper.
     *
     * @param a  - The first EventNode
     * @param oa - The occurence point of the first EventNode.
     * @param b  - The second EventNode
     * @param ob - The occurence point of the second EventNode.
     * @return <0 If a is before b, >0 if b is before a, 0 if equal.
     */
    private static int compare(EventNode a, double oa, EventNode b, double ob) {

        //1 time
        int timeComparison = Double.compare(oa, ob);
        if (timeComparison != 0) {
            return timeComparison;
        }

        //remainder is about event nodes themselves
        return EventNode.compare(a, b);
    }
}
