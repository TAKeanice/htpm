package de.dbvis.htpm;

import de.dbvis.htpm.constraints.HTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder;
import de.dbvis.htpm.htp.HTPUtils;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalEndEventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalStartEventNode;
import de.dbvis.htpm.htp.eventnodes.PointEventNode;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.util.HTPMListener;
import de.dbvis.htpm.util.HTPMOutputEvent;
import de.dbvis.htpm.util.HTPMOutputListener;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * @author Wolfgang Jentner
 *
 */
public class HTPM implements TemporalPatternProducer {

	private final int threadPoolSize;
	private final boolean parallel;
	private final boolean saveMemory;

	private ExecutorService es;

	/**
	 * The database the HTPM operates on
	 */
	protected HybridEventSequenceDatabase d;

	/**
	 * the constraints for determining which patterns or occurrences to join,
	 * and which patterns or occurrences to prune
	 */
	protected final HTPMConstraint constraint;
	
	/**
	 * One list per level, consisting of lists of search space partitions, which hold the resulting patterns
	 */
	protected List<List<List<PatternOccurrence>>> patterns;

	protected final List<HTPMListener> listeners;
	
	/**
	 * Creates a new HTPM-Algorithm-Object.
	 * @param d - The Database containing the series.
	 * @param constraint - The constraint determining the pre- and post-joining pruning behavior.
	 */
	public HTPM(HybridEventSequenceDatabase d, HTPMConstraint constraint) {
		this(d, constraint, false, 1);
	}

	public HTPM(HybridEventSequenceDatabase d, HTPMConstraint constraint, boolean saveMemory, int threadPoolSize) {

		if(d == null) {
			throw new IllegalArgumentException("HybridEventDatabase must not be null");
		}
		if (threadPoolSize < 1) {
			throw new IllegalArgumentException("There must be minimum 1 thread to run on");
		}

		this.listeners = new LinkedList<>();
		this.parallel = threadPoolSize > 1;

		this.d = d;
		this.constraint = constraint;
		this.saveMemory = saveMemory;
		this.threadPoolSize = threadPoolSize;
	}

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
			final HybridEvent op1 = or1.get(i1);
			final HybridEvent op2 = or2.get(i2);
			final EventNode n1 = pa1.get(i1);
			final EventNode n2 = pa2.get(i2);
			final EventNode nP = pre.size() > ip ? pre.get(ip) : null;

			if (n1.equals(nP) && n2.equals(nP) && op1.equals(op2)) {
				//both nodes are part of the "prefix", so it does not matter what we append
				b.append(0, n1, op1);
				i1++;
				i2++;
				ip++;
			} else if (HTPUtils.compareOccurrencePoints(op1, n1, op2, n2, true) < 0) {
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
	 * Returns the resulting patterns that satisfy the previously set 
	 * minimum support and the occurrences they have.
	 * May be null if the algorithm is not executed before. Otherwise it will always
	 * return the patterns of the last run.If there are no frequent patterns
	 * the algorithm will return an empty map.
	 * 
	 * @return The patterns with their occurrences
	 */
	public Map<HybridTemporalPattern, Set<Occurrence>> getPatterns() {
		if (saveMemory) {
			throw new RuntimeException("Patterns are not saved after output in low storage mode!");
		}
		if(this.patterns == null) {
			return Collections.emptyMap();
		}
		final Stream<PatternOccurrence> poStream = this.patterns.stream().flatMap(Collection::stream).flatMap(Collection::stream);
		return filterBeforeOutput(poStream)
				.collect(Collectors.toMap(po -> po.pattern, po -> po.occurrences.stream()
						.map(link -> link.child).collect(Collectors.toSet())));
	}

	/**
	 * Returns the resulting patterns that satisfy the previously set 
	 * minimum support and the occurrences they have.
	 * The map is sorted by the length of the pattern.
	 * May be null if the algorithm is not executed before. Otherwise it will always
	 * return the patterns of the last run. If there are no frequent patterns
	 * the algorithm will return an empty map.
	 * 
	 * @return The patterns sorted by length with their occurrences
	 */
	public Map<HybridTemporalPattern, Set<Occurrence>> getPatternsSortedByLength() {
		TreeMap<HybridTemporalPattern, Set<Occurrence>> sortedmap = new TreeMap<>(HybridTemporalPattern::compareTo);
		sortedmap.putAll(getPatterns());
		return sortedmap;
	}

	@Override
	public void addHTPMListener(HTPMListener l) {
		if(l == null) {
			return;
		}
		this.listeners.add(l);
	}

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

	void output(List<List<PatternOccurrence>> patterns, int depth) {
		final Stream<HTPMOutputEvent.PatternOccurrence> outputPatterns =
				filterBeforeOutput(patterns.stream().flatMap(Collection::stream))
						.map(po -> new HTPMOutputEvent.PatternOccurrence(
								po.pattern,
								po.occurrences.stream().map(link -> link.child).collect(Collectors.toSet())));
		this.fireHTPMEvent(new HTPMOutputEvent(this, depth, patterns.stream().mapToInt(List::size).sum(), outputPatterns));
	}

	private Stream<PatternOccurrence> filterBeforeOutput(Stream<PatternOccurrence> patternOccurrenceStream) {
		return patternOccurrenceStream
				//filter occurrences
				.map(po -> new PatternOccurrence(po.prefix, po.pattern,
						po.occurrences.stream()
								.filter(occ -> constraint.shouldOutputOccurrence(po.pattern, occ.child))
								.collect(Collectors.toList())))
				//filter patterns (new occurrence lists can lead to even more patterns filtered)
				.filter(po -> constraint.shouldOutputPattern(po.pattern,
						po.occurrences.stream()
								.map(link -> link.child)
								.collect(Collectors.toSet())));
	}

	//================================================================================
	// The actual algorithm
	//================================================================================

	/**
	 * The method that starts the algorithm.
	 */
	@Override
	public void start() {

		if (parallel) {
			es = Executors.newFixedThreadPool(threadPoolSize);
		}

		if (!saveMemory) {
			this.patterns = new ArrayList<>();
		}

		if (!constraint.shouldGeneratePatternsOfLength(1)) {
			return;
		}

		List<List<PatternOccurrence>> m = this.genL1();

		if (!saveMemory) {
			this.patterns.add(m);
		}

		int totalNumPatterns = m.get(0).size();
		output(m, 1);

		int k = 2;

		while(totalNumPatterns > 1 && constraint.shouldGeneratePatternsOfLength(k)) {
			m = this.genLk(m, k);

			if (!saveMemory) {
				this.patterns.add(m);
			}

			totalNumPatterns = m.stream().mapToInt(List::size).sum();
			output(m, k);

			k++;
		}

		if (parallel) {
			try {
				es.shutdown();
				es.awaitTermination(10, TimeUnit.SECONDS);
			} catch (Exception e) {
				throw new RuntimeException("Interrupted while waiting for execution to end", e);
			}
		}
	}
	
	/**
	 * Generates the first generation (1-patterns) out of the previously given database.
	 * @return Returns the first generation of patterns that already satisfy all constraints.
	 */
	protected List<List<PatternOccurrence>> genL1() {
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
	 * @return Returns a map of all patterns that satisfy all constraints. In addition all occurences of each pattern.
	 */
	protected List<List<PatternOccurrence>> genLk(final List<List<PatternOccurrence>> partitionedOccurrences, int k) {

		List<List<Map<HybridTemporalPattern, PatternOccurrence>>> partitionResults = new ArrayList<>(partitionedOccurrences.size());

		//AtomicInteger joined = new AtomicInteger(0);

		List<Callable<Void>> joinCallables = new ArrayList<>();

		for (int partition = 0; partition < partitionedOccurrences.size(); partition++) {
			List<PatternOccurrence> joinablePatterns = partitionedOccurrences.get(partition);

			if (!constraint.branchCanProduceResults(joinablePatterns)) {
				//go to next partition if this one cannot deliver appropriate patterns
				continue;
			}

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
						if (!constraint.patternsQualifyForJoin(first.prefix, first.pattern, second.pattern, k)) {
							continue;
						}

						final List<Map<HybridTemporalPattern, PatternOccurrence>> joined = join(first, second, k);
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
					joinCallables.add(() -> {
						join.run();
						return null;
					});
				} else {
					join.run();
				}
			}
		}

		if (parallel) {
			try {
				es.invokeAll(joinCallables);
			} catch (InterruptedException e) {
				throw new RuntimeException("interrupted while generating patterns of size " + k, e);
			}
		}

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
	 * @param patternOccurrence1 the first pattern and its occurrences to be joined
	 * @param patternOccurrence2 the second pattern and its occurrences to be joined
	 * @param k the generation number (length of patterns to be generated)
	 * @return Returns two maps of patterns that satisfy the min-support and the desired length.
	 * For each pattern a complete map of its occurences will be returned.
	 * The first map are the patterns with parent p1, the second one those with parent p2
	 */
	protected List<Map<HybridTemporalPattern, PatternOccurrence>> join(PatternOccurrence patternOccurrence1,
																	   PatternOccurrence patternOccurrence2,
																	   int k) {

		HybridTemporalPattern prefix = patternOccurrence1.prefix;

		HybridTemporalPattern p1 = patternOccurrence1.pattern;
		HybridTemporalPattern p2 = patternOccurrence2.pattern;

		List<PatternOccurrence.OccurrenceTreeLink> or1 = patternOccurrence1.occurrences;
		List<PatternOccurrence.OccurrenceTreeLink> or2 = patternOccurrence2.occurrences;

		//heuristic: the occurrence records are joined, from each pair in the same sequence we can have a new one.
		// assumption is that the number of occurrences is evenly distributed over sequences (which reduces the result)
		// and that all joins yield the same pattern (which increases the result).
		// additionally we assume that both joined patterns have the same likelyhood to be the parent pattern
		final int newOccurrenceCountHeuristic = (or1.size() / (d.size() * 2)) * (or2.size() / (d.size() * 2));

		final List<Map<HybridTemporalPattern, PatternOccurrence>> partitionedResult = new ArrayList<>(2);
		final Map<HybridTemporalPattern, PatternOccurrence> parentP1 = new HashMap<>();
		final Map<HybridTemporalPattern, PatternOccurrence> parentP2 = new HashMap<>();
		partitionedResult.add(parentP1);
		partitionedResult.add(parentP2);

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

			for (int i2 = 0; i2 <= maxI2; i2++) {
				//for (int i2 = minI2; i2 < or2.size(); i2++) {
				final PatternOccurrence.OccurrenceTreeLink link2 = or2.get(i2);
				final Occurrence occurrencePrefix2 = link2.parent;
				Occurrence s2 = link2.child;

				if (occurrencePrefix1 != occurrencePrefix2
						|| !constraint.occurrenceRecordsQualifyForJoin(p1, s1, p2, s2, k)) {
					continue;
				}

				DefaultHybridTemporalPatternBuilder b = ORAlign(prefix, p1, s1, p2, s2, k);
				HybridTemporalPattern newPattern = b.getPattern();
				HybridTemporalPattern newPatternPrefix = b.getPatternPrefix();
				Occurrence newOccurrence = b.getOccurence();

				//prune new occurrence records
				if (constraint.newOccurrenceFulfillsConstraints(newPattern, newOccurrence, k)) {
					Map<HybridTemporalPattern, PatternOccurrence> map = newPatternPrefix == p1 ? parentP1 : parentP2;
					map.computeIfAbsent(
							//initialize new arraylist for occurrences of the pattern
							//newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, new ArrayList<>()))
							//initialize new linked list for occurrences of the pattern
							//newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, new LinkedList<>()))
							//initialize array list with heuristically determined capacity
							newPattern, p -> new PatternOccurrence(newPatternPrefix, newPattern, newOccurrenceCountHeuristic))
							.occurrences.add(new PatternOccurrence.OccurrenceTreeLink(b.getOccurrencePrefix(), newOccurrence));
				}
			}
		}

		//prune new patterns
		parentP1.entrySet().removeIf(e -> !constraint.patternFulfillsConstraints(e.getKey(),
				e.getValue().occurrences.stream().map(link -> link.child).collect(Collectors.toSet()), k));
		parentP2.entrySet().removeIf(e -> !constraint.patternFulfillsConstraints(e.getKey(),
				e.getValue().occurrences.stream().map(link -> link.child).collect(Collectors.toSet()), k));

		//convert linkedlists into arraylists for better performance later
		//parentP1.entrySet().forEach(e -> e.setValue(new ArrayList<>(e.getValue())));
		//parentP2.entrySet().forEach(e -> e.setValue(new ArrayList<>(e.getValue())));

		//shrink arraylists allocated with large amount of memory
		parentP1.forEach((key, value) -> ((ArrayList) value.occurrences).trimToSize());
		parentP2.forEach((key, value) -> ((ArrayList) value.occurrences).trimToSize());

		return partitionedResult;
	}
}
