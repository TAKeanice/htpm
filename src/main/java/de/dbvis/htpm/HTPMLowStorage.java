package de.dbvis.htpm;

import de.dbvis.htpm.constraints.HTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalEndEventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalStartEventNode;
import de.dbvis.htpm.htp.eventnodes.PointEventNode;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.DefaultOccurrencePoint;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.util.HTPMListener;
import de.dbvis.htpm.util.HTPMOutputEvent;
import de.dbvis.htpm.util.HTPMOutputListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is the core class which actually contains the HTPM-Algorithm.
 * It finds patterns and their occurrences that occur frequently and
 * satisfy a given minimum support.
 *
 * The framework basically implements the algorithm that is proposed in this paper:
 * <br/>Title: Discovering hybrid temporal patterns from sequences consisting of point- and interval-based events
 * <br/>Authors: Shin-Yi Wu, Yen-Liang Chen
 * <br/>Published: 2009
 * <br/><br/>
 * However, I found some edge cases that the paper does not describe
 * and tried to solve these to the best of my knowledge.
 * I also used some simple heuristics to improve the runtime a little bit.
 *
 * This version of the algorithm only stores the patterns relevant for proceeding the algorithm,
 * and outputs all others to the listener. This way, storage space for running the algorithm is reduced.
 * Of course, this only makes a difference if the listener does not keep all patterns in memory.
 * The child -> parent relationships between children and their canonical prefixes are not output.
 *
 * @author Wolfgang Jentner, Tassilo Karge
 *
 */
public class HTPMLowStorage implements Runnable {

    private final boolean parallel = true;
    private final int threadPoolSize = 10;

    /**
     * The database the HTPM operates on
     */
    protected HybridEventSequenceDatabase d;

    /**
     * the constraints for determining which patterns or occurrences to join,
     * and which patterns or occurrences to prune
     */
    protected final HTPMConstraint constraint;

    protected final List<HTPMListener> listeners;

    /**
     * Creates a new HTPM-Algorithm-Object.
     * @param d - The Database containing the series.
     * @param constraint - The constraint determining the pre- and post-joining pruning behavior.
     */
    public HTPMLowStorage(HybridEventSequenceDatabase d, HTPMConstraint constraint) {

        if(d == null) {
            throw new NullPointerException("HybridEventDatabase must not be null");
        }
        this.d = d;
        this.constraint = constraint;

        this.listeners = new LinkedList<>();
    }

    /**
     * The method that actually runs the algorithm.
     */
    @Override
    public void run() {

        if (!constraint.shouldGeneratePatternsOfLength(1)) {
            return;
        }

        List<List<PatternOccurrence>> m;

        m = this.genL1();

        int totalNumPatterns = m.get(0).size();
        this.fireHTPMEvent(new HTPMOutputEvent(this, 1, totalNumPatterns,
                m.stream().flatMap(Collection::stream).collect(
                        Collectors.toMap(po -> po.pattern, po -> po.occurrences.stream().map(link -> link.child)
                                .collect(Collectors.toList())))));

        int k = 2;

        try {
            while(totalNumPatterns > 1 && constraint.shouldGeneratePatternsOfLength(k)) {
                m = this.genLk(m, k);

                totalNumPatterns = m.stream().mapToInt(List::size).sum();
                this.fireHTPMEvent(new HTPMOutputEvent(this, k, totalNumPatterns,
                        m.stream().flatMap(Collection::stream).collect(
                                Collectors.toMap(po -> po.pattern, po -> po.occurrences.stream().map(link -> link.child)
                                        .collect(Collectors.toList())))));

                k++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an HTPMListener.
     * @param l - the HTPMListener to be added.
     */
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
    public void removeHTPMListener(HTPMListener l) {
        this.listeners.remove(l);
    }

    /**
     * Fires an HTPMEvent to all the current listeners.
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

    /**
     * Generates the first generation out of the previously given database.
     * @return Returns the first generation of patterns that already satisfy the min-support.
     */
    protected List<List<PatternOccurrence>> genL1() {
        Map<HybridTemporalPattern, List<OccurrenceTreeLink>> map =
                new HashMap<>();

        for(HybridEventSequence seq : d.getSequences()) {

            Occurrence emptyOccurrencePrefix = new DefaultOccurrence(seq, Collections.emptyList());

            for(HybridEvent e : seq.getEvents()) {

                DefaultHybridTemporalPatternBuilder builder = new DefaultHybridTemporalPatternBuilder(seq, 1);

                if (e.isPointEvent()) {
                    builder.append(0, new PointEventNode(e.getEventId()), new DefaultOccurrencePoint(e));
                } else {
                    builder.append(0, new IntervalStartEventNode(e.getEventId(), 0),
                            new DefaultOccurrencePoint(e, true));
                    builder.append(0, new IntervalEndEventNode(e.getEventId(), 0),
                            new DefaultOccurrencePoint(e, false));
                }

                //set empty occurrence as prefix
                Occurrence oc = builder.getOccurence(false);
                HybridTemporalPattern p = builder.getPattern(false);

                if (constraint.newOccurrenceFulfillsConstraints(p, oc, 1)) {
                    map.computeIfAbsent(p, pattern -> new ArrayList<>()).add(new OccurrenceTreeLink(oc, emptyOccurrencePrefix));
                }
            }
        }

        //prune unsupported patterns
        map.entrySet().removeIf(entry -> !constraint.patternFulfillsConstraints(entry.getKey(),
                entry.getValue().stream().map(occurrenceTreeLink -> occurrenceTreeLink.child).collect(Collectors.toList()), 1));

        //parse maps into patternOccurrence objects
        final List<PatternOccurrence> patternOccurrences = map.entrySet().stream().map(entry ->
                new PatternOccurrence(null, entry.getKey(), entry.getValue())).collect(Collectors.toList());

        //level 1: all patterns have the same parent, so they belong to the same partition
        return Collections.singletonList(patternOccurrences);
    }

    /**
     * Joins a generation of patterns according to definition 10.
     * @param partitionedOccurrences - The current generation of patterns, partitioned by pattern parent.
     * @param k the generation number (length of patterns to be generated)
     * @return Returns a map of all patterns that satisfy the minimum support. In addition all occurence series of each pattern are returned.
     */
    protected List<List<PatternOccurrence>> genLk(final List<List<PatternOccurrence>> partitionedOccurrences, int k) throws InterruptedException {

        List<List<Map<HybridTemporalPattern, PatternOccurrence>>> partitionResults = new ArrayList<>(partitionedOccurrences.size());

        ExecutorService es = Executors.newFixedThreadPool(threadPoolSize);

        //AtomicInteger joined = new AtomicInteger(0);

        for (int partition = 0; partition < partitionedOccurrences.size(); partition++) {
            List<PatternOccurrence> joinablePatterns = partitionedOccurrences.get(partition);

            List<Map<HybridTemporalPattern, PatternOccurrence>> partitionResult = new ArrayList<>(joinablePatterns.size());
            partitionResults.add(partitionResult);

            for (int i = 0; i < joinablePatterns.size(); i++) {
                if (parallel) {
                    partitionResult.add(new ConcurrentHashMap<>());
                    //partitionResult.add(Collections.synchronizedMap(new HashMap<>()));
                } else {
                    partitionResult.add(new HashMap<>());
                }
            }

            for (int i = 0; i < joinablePatterns.size(); i++) {
                final PatternOccurrence first = joinablePatterns.get(i);

                final int finalI = i;

                final Runnable join = () -> {

                    List<Map<HybridTemporalPattern, PatternOccurrence>> subResult = new ArrayList<>(finalI + 1);
                    for (int j = 0; j <= finalI; j++) {
                        subResult.add(new HashMap<>());
                    }

                    for (int j = 0; j <= finalI; j++) {
                        final PatternOccurrence second = joinablePatterns.get(j);

                        //only join qualifying patterns
                        if (!constraint.patternsQualifyForJoin(first.pattern, second.pattern, k)) {
                            continue;
                        }

                        final List<Map<HybridTemporalPattern, PatternOccurrence>> joined =
                                HTPMLowStorage.this.join(first.prefix, first.pattern, first.occurrences,
                                        second.pattern, second.occurrences,
                                        k);
                        subResult.get(finalI).putAll(joined.get(0));
                        subResult.get(j).putAll(joined.get(1));
                        //System.out.println("joined " + joined.incrementAndGet() + " with " + l1.size() + " and " + l2.size() + " occurrences.");
                    }

                    //merge results from one run into results of complete partition
                    for (int j = 0; j < subResult.size(); j++) {
                        partitionResult.get(j).putAll(subResult.get(j));
                    }
                };

                if (parallel) {
                    es.execute(join);
                } else {
                    join.run();
                }
            }
        }

        es.shutdown();

        es.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        //Flatten maps into PatternOccurrence list
        return partitionResults.stream()
                .flatMap(partitionResult -> partitionResult.stream()
                        .map(map -> new ArrayList<>(map.values())))
                .collect(Collectors.toList());
    }

    /**
     * This method joins two patterns with all of their occurences.
     * It will probably return more than one resulting pattern because of the
     * occurence points.
     * @param prefix - The prefix of the two patterns, can be NULL too.
     * @param p1 - The first pattern.
     * @param or1 - All occurences of the first pattern.
     * @param p2 - The second pattern.
     * @param or2 - All occurences of the second pattern.
     * @param k the generation number (length of patterns to be generated)
     * @return Returns two maps of patterns that satisfy the min-support and the desired length.
     * For each pattern a complete map of its occurences will be returned.
     * The first map are the patterns with parent p1, the second one those with parent p2
     */
    protected List<Map<HybridTemporalPattern, PatternOccurrence>> join(final HybridTemporalPattern prefix,
                                                                       final HybridTemporalPattern p1, final List<OccurrenceTreeLink> or1,
                                                                       final HybridTemporalPattern p2, final List<OccurrenceTreeLink> or2,
                                                                       int k) {

        //heuristic: the occurrence records are joined, from each pair in the same sequence we can have a new one.
        // assumption here is that the number of occurrences is evenly distributed over sequences (which reduces the result)
        // and that all joins yield the same pattern (which increases the result)
        final int newOccurrenceCountHeuristic = or1.size() * or2.size() / (d.size() * d.size());

        final List<Map<HybridTemporalPattern, PatternOccurrence>> partitionedResult = new ArrayList<>(2);
        final Map<HybridTemporalPattern, PatternOccurrence> parentP1 = new HashMap<>();
        final Map<HybridTemporalPattern, PatternOccurrence> parentP2 = new HashMap<>();
        partitionedResult.add(parentP1);
        partitionedResult.add(parentP2);

        //int i1 = -1;
        //for (Occurrence s1 : or1) {
        //	i1++;
        for (int i1 = 0; i1 < or1.size(); i1++) {
            Occurrence occurrencePrefix = or1.get(i1).parent;
            Occurrence s1 = or1.get(i1).child;

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
                Occurrence s2 = or2.get(i2).child;
                if (occurrencePrefix != or2.get(i2).parent || !constraint.occurrenceRecordsQualifyForJoin(s1, s2, k)) {
                    continue;
                }

                DefaultHybridTemporalPatternBuilder b = ORAlignment.ORAlign(prefix, p1, s1, p2, s2, k);
                HybridTemporalPattern newPattern = b.getPattern(false);
                HybridTemporalPattern newPatternPrefix = b.getPatternPrefix();
                Occurrence newOccurrence = b.getOccurence(false);

                //prune new occurrence records
                if (constraint.newOccurrenceFulfillsConstraints(newPattern, newOccurrence, k)) {
                    Map<HybridTemporalPattern, PatternOccurrence> map = newPatternPrefix == p1 ? parentP1 : parentP2;
                    //initialize with capacity as the average number per sequence of ORs multiplied
                    map.computeIfAbsent(
                            //newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, new ArrayList<>()))
                            //newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, new LinkedList<>()))
                            newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, newOccurrenceCountHeuristic))
                            .occurrences.add(new OccurrenceTreeLink(newOccurrence, b.getOccurrencePrefix()));
                }
            }
        }

        //prune new patterns
        parentP1.entrySet().removeIf(e -> !constraint.patternFulfillsConstraints(e.getKey(),
                e.getValue().occurrences.stream().map(link -> link.child).collect(Collectors.toList()), k));
        parentP2.entrySet().removeIf(e -> !constraint.patternFulfillsConstraints(e.getKey(),
                e.getValue().occurrences.stream().map(link -> link.child).collect(Collectors.toList()), k));

        //convert linkedlists into arraylists for better performance later
        //parentP1.entrySet().forEach(e -> e.setValue(new ArrayList<>(e.getValue())));
        //parentP2.entrySet().forEach(e -> e.setValue(new ArrayList<>(e.getValue())));

        //shrink arraylists allocated with large amount of memory
        parentP1.forEach((key, value) -> ((ArrayList) value.occurrences).trimToSize());
        parentP2.forEach((key, value) -> ((ArrayList) value.occurrences).trimToSize());

        return partitionedResult;
    }

    /**
     * This method compares two event nodes. The comparison is made according to the
     * definition 6 (Arrangement of event nodes in htp). in the paper.
     * @param a - The first EventNode
     * @param oa - The occurence point of the first EventNode.
     * @param b - The second EventNode
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


    class PatternOccurrence {
        final HybridTemporalPattern prefix;
        final HybridTemporalPattern pattern;
        final List<OccurrenceTreeLink> occurrences;

        PatternOccurrence(HybridTemporalPattern prefix, HybridTemporalPattern pattern, int initialListSize) {
            this.prefix = prefix;
            this.pattern = pattern;
            this.occurrences = new ArrayList<>(initialListSize);
        }

        PatternOccurrence(HybridTemporalPattern prefix, HybridTemporalPattern pattern, List<OccurrenceTreeLink> occurrences) {
            this.prefix = prefix;
            this.pattern = pattern;
            this.occurrences = occurrences;
        }
    }

    class OccurrenceTreeLink {
        final Occurrence child;
        final Occurrence parent;

        OccurrenceTreeLink(Occurrence child, Occurrence parent) {
            this.child = child;
            this.parent = parent;
        }
    }
}