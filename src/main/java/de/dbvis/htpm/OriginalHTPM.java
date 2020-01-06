package de.dbvis.htpm;

import de.dbvis.htpm.constraints.AgrawalSupportConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.IntervalEndEventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalStartEventNode;
import de.dbvis.htpm.htp.eventnodes.PointEventNode;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.util.HTPMListener;
import de.dbvis.htpm.util.HTPMOutputEvent;
import de.dbvis.htpm.util.HTPMOutputListener;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OriginalHTPM implements TemporalPatternProducer {

    protected final List<HTPMListener> listeners = new LinkedList<>();

    private final HybridEventSequenceDatabase d;
    private final AgrawalSupportConstraint constraint;

    OriginalHTPM(HybridEventSequenceDatabase d, double minSupport) {
        this.d = d;
        constraint = new AgrawalSupportConstraint(d.size(), minSupport);
    }

    /**
     * The method that starts the algorithm.
     */
    @Override
    public void start() {

        List<PatternOccurrence> m = this.genL1();

        int totalNumPatterns = m.size();
        output(m, 1);

        int k = 2;

        while(totalNumPatterns > 1 && constraint.shouldGeneratePatternsOfLength(k)) {
            m = this.genLk(m, k);

            totalNumPatterns = m.size();
            output(m, k);

            k++;
        }
    }

    /**
     * Generates the first generation (1-patterns) out of the previously given database.
     * @return Returns the first generation of patterns that already satisfy all constraints.
     */
    protected List<PatternOccurrence> genL1() {
        Map<HybridTemporalPattern, List<PatternOccurrence.OccurrenceTreeLink>> map =
                new HashMap<>();

        for(HybridEventSequence seq : d.getSequences()) {

            Occurrence emptyOccurrencePrefix = new DefaultOccurrence(seq, Collections.emptyList());

            for(HybridEvent e : seq.getEvents()) {

                DefaultHybridTemporalPatternBuilder builder = new DefaultHybridTemporalPatternBuilder(seq, 1);

                if (e.isPointEvent()) {
                    builder.append(0, new PointEventNode(e.getEventId()), e);
                } else {
                    builder.append(0, new IntervalStartEventNode(e.getEventId(), 0), e);
                    builder.append(0, new IntervalEndEventNode(e.getEventId(), 0), e);
                }

                Occurrence occ = builder.getOccurence();
                HybridTemporalPattern p = builder.getPattern();

                //set empty occurrence as prefix
                if (constraint.newOccurrenceFulfillsConstraints(p, occ, 1)) {
                    map.computeIfAbsent(p, pattern -> new ArrayList<>())
                            .add(new PatternOccurrence.OccurrenceTreeLink(emptyOccurrencePrefix, occ));
                }
            }
        }

        //prune unsupported patterns
        map.entrySet().removeIf(entry -> !constraint.patternFulfillsConstraints(entry.getKey(),
                entry.getValue().stream().map(link -> link.child).collect(Collectors.toSet()), 1));

        //convert maps into patternOccurrence objects
        return map.entrySet().stream().map(entry ->
                new PatternOccurrence(null, entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    /**
     * Joins a generation of patterns according to definition 10.
     * @param patternOccurrences - The current generation of patterns, partitioned by pattern parent.
     * @param k the generation number (length of patterns to be generated)
     * @return Returns a map of all patterns that satisfy all constraints. In addition all occurences of each pattern.
     */
    protected List<PatternOccurrence> genLk(final List<PatternOccurrence> patternOccurrences, int k) {

        List<PatternOccurrence> allJoined = new ArrayList<>();

        for (int i = 0; i < patternOccurrences.size(); i++) {
            final PatternOccurrence first = patternOccurrences.get(i);

            for (int j = 0; j <= i; j++) {
                final PatternOccurrence second = patternOccurrences.get(j);

                if (first.prefix != second.prefix) {
                    continue;
                }

                Collection<PatternOccurrence> joined = join(first, second, k);
                allJoined.addAll(joined);
            }

        }

        return allJoined;
    }

    /**
     * This method joins two patterns with all of their occurences.
     * It will probably return more than one resulting pattern because of the
     * occurence points.
     * @param patternOccurrence1 the first pattern and its occurrences to be joined
     * @param patternOccurrence2 the second pattern and its occurrences to be joined
     * @param k the generation number (length of patterns to be generated)
     * @return Returns two maps of patterns that satisfy the min-support and the desired length.
     * For each pattern a complete map of its occurences will be returned.
     * The first map are the patterns with parent p1, the second one those with parent p2
     */
    protected Collection<PatternOccurrence> join(PatternOccurrence patternOccurrence1,
                                           PatternOccurrence patternOccurrence2,
                                           int k) {

        HybridTemporalPattern prefix = patternOccurrence1.prefix;

        HybridTemporalPattern p1 = patternOccurrence1.pattern;
        HybridTemporalPattern p2 = patternOccurrence2.pattern;

        List<PatternOccurrence.OccurrenceTreeLink> or1 = patternOccurrence1.occurrences;
        List<PatternOccurrence.OccurrenceTreeLink> or2 = patternOccurrence2.occurrences;

        //heuristic: the occurrence records are joined, from each pair in the same sequence we can have a new one.
        // assumption here is that the number of occurrences is evenly distributed over sequences (which reduces the result)
        // and that all joins yield the same pattern (which increases the result)
        final int newOccurrenceCountHeuristic = or1.size() * or2.size() / (d.size() * d.size());

        final Map<HybridTemporalPattern, PatternOccurrence> result = new HashMap<>(2);

        //int i1 = -1;
        //for (Occurrence s1 : or1) {
        //	i1++;
        for (int i1 = 0; i1 < or1.size(); i1++) {
            final PatternOccurrence.OccurrenceTreeLink link1 = or1.get(i1);
            Occurrence occurrencePrefix1 = link1.parent;
            Occurrence s1 = link1.child;

            //avoid join of same occurrences twice (happens if both are from the same occurrence record)
            //int minI2 = or1 == or2 ? i1 + 1 : 0;
            int maxI2 = or1 == or2 ? i1 - 1 : or2.size() - 1;

            //int i2 = -1;
            //for (Occurrence s2 : or2) {
            //	i2++;
            //	//if (i2 < minI2) {
            //	//	continue;
            //	//}
            //	if (i2 > maxI2) {
            //		break;
            //	}
            for (int i2 = 0; i2 <= maxI2; i2++) {
                //for (int i2 = minI2; i2 < or2.size(); i2++) {
                final PatternOccurrence.OccurrenceTreeLink link2 = or2.get(i2);
                final Occurrence occurrencePrefix2 = link2.parent;
                Occurrence s2 = link2.child;

                if (occurrencePrefix1 != occurrencePrefix2 || !constraint.occurrenceRecordsQualifyForJoin(p1, s1, p2, s2, k)) {
                    continue;
                }

                DefaultHybridTemporalPatternBuilder b = HTPM.ORAlign(prefix, p1, s1, p2, s2, k);
                HybridTemporalPattern newPattern = b.getPattern();
                HybridTemporalPattern newPatternPrefix = b.getPatternPrefix();
                Occurrence newOccurrence = b.getOccurence();

                //prune new occurrence records
                //initialize with capacity as the average number per sequence of ORs multiplied
                result.computeIfAbsent(
                        //newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, new ArrayList<>()))
                        //newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, new LinkedList<>()))
                        newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, newOccurrenceCountHeuristic))
                        .occurrences.add(new PatternOccurrence.OccurrenceTreeLink(b.getOccurrencePrefix(), newOccurrence));
            }
        }

        //prune new patterns
        result.entrySet().removeIf(e -> !constraint.patternFulfillsConstraints(e.getKey(),
                e.getValue().occurrences.stream().map(link -> link.child).collect(Collectors.toSet()), k));

        //convert linkedlists into arraylists for better performance later
        //parentP1.entrySet().forEach(e -> e.setValue(new ArrayList<>(e.getValue())));
        //parentP2.entrySet().forEach(e -> e.setValue(new ArrayList<>(e.getValue())));

        //shrink arraylists allocated with large amount of memory
        result.forEach((key, value) -> ((ArrayList) value.occurrences).trimToSize());

        return result.values();
    }

    /**
     * Adds an HTPMListener, which receives update events about the pattern mining process.
     * @param l - the HTPMListener to be added.
     */
    @Override
    public void addHTPMListener(HTPMListener l) {
        if(l == null) {
            return;
        }
        this.listeners.add(l);
    }

    /**
     * Removes an HTPMListener.
     * @param l - the HTPMListener to be removed.
     */
    @Override
    public void removeHTPMListener(HTPMListener l) {
        this.listeners.remove(l);
    }

    /**
     * Fires an update to all the current listeners.
     * @param e - the HTPMEvent to be fired.
     */
    protected void fireHTPMEvent(HTPMOutputEvent e) {
        for(HTPMListener l : this.listeners) {
            l.generationCalculated(e);

            if (l instanceof HTPMOutputListener) {
                ((HTPMOutputListener) l).outputGenerated(e);
            }
        }
    }

    protected void output(List<PatternOccurrence> patterns, int depth) {
        final Stream<HTPMOutputEvent.PatternOccurrence> outputPatterns =
                patterns.stream().map(po -> new HTPMOutputEvent.PatternOccurrence(
                                po.pattern,
                                po.occurrences.stream().map(link -> link.child).collect(Collectors.toSet())));
        this.fireHTPMEvent(new HTPMOutputEvent(this, depth, patterns.size(), outputPatterns));
    }
}
