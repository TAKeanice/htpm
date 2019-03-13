package de.dbvis.htpm;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.*;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.DefaultOccurrencePoint;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.occurrence.OccurrencePoint;
import de.dbvis.htpm.util.HTPMEvent;
import de.dbvis.htpm.util.HTPMListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
public class HTPM implements Runnable {

	private final boolean parallel = false;

	/**
	 * The database the HTPM operates on
	 */
	protected HybridEventSequenceDatabase d;
	
	/**
	 * The minimum support each pattern has to satisfy
	 */
	protected double min_sup;
	
	/**
	 * The resulting patterns
	 */
	protected Map<HybridTemporalPattern, List<Occurrence>> patterns;

	protected List<HTPMListener> listeners;
	
	/**
	 * Creates a new HTPM-Algorithm-Object.
	 * @param d - The Database containing the series.
	 * @param min_support - The minimum support.
	 */
	public HTPM(HybridEventSequenceDatabase d, double min_support) {
		if(d == null) {
			throw new NullPointerException("HybridEventDatabase must not be null");
		}
		this.d = d;
		this.setMinimumSupport(min_support);
		
		this.listeners = new LinkedList<>();
	}
	
	/**
	 * Sets a different minimum support.
	 * 
	 * @param min_support - The minimum support
	 */
	public void setMinimumSupport(double min_support) {
		if(min_support <= 0 || min_support > 1) {
			throw new IllegalArgumentException("Minimum support must be 0 < min_support <= 1");
		}
		this.min_sup = min_support;
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
	public Map<HybridTemporalPattern, List<Occurrence>> getPatterns() {
		if(this.patterns == null) {
			return null;
		}
		return Collections.unmodifiableMap(this.patterns);
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
	public Map<HybridTemporalPattern, List<Occurrence>> getPatternsSortedByLength() {
		TreeMap<HybridTemporalPattern, List<Occurrence>> sortedmap = new TreeMap<>((o1, o2) -> {
            if (o1.length() == o2.length()) {
                return o1.toString().compareTo(o2.toString());
            }

            return o1.length() - o2.length();
        });
		
		sortedmap.putAll(this.patterns);
		
		return Collections.unmodifiableMap(sortedmap);
	}
	
	/**
	 * The method that actually runs the algorithm.
	 */
	@Override
	public void run() {
		this.patterns = new HashMap<>();
		
		Map<HybridTemporalPattern, List<Occurrence>> m;
		
		m = this.genL1();
		
		this.patterns.putAll(m);
		
		this.fireHTPMEvent(new HTPMEvent(this, 1, m.keySet().size()));
		
		int k = 2;

		try {
			while(m.keySet().size() > 1) {
				m = this.genLk(m, k);
				this.patterns.putAll(m);
				this.fireHTPMEvent(new HTPMEvent(this, k, m.keySet().size()));
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
	protected void fireHTPMEvent(HTPMEvent e) {
		for(HTPMListener l : this.listeners) {
			l.generationCalculated(e);
		}
	}
	
	/**
	 * Generates the first generation out of the previously given database.
	 * @return Returns the first generation of patterns that already satisfy the min-support.
	 */
	protected Map<HybridTemporalPattern, List<Occurrence>> genL1() {
		Map<HybridTemporalPattern, List<Occurrence>> map =
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

				builder.setPrefixes(null, emptyOccurrencePrefix);

				//set empty occurrence as prefix
				Occurrence oc = builder.getOccurence();
				HybridTemporalPattern p = builder.getPattern();

				if(!map.containsKey(p)) {
					map.put(p, new ArrayList<>());
				}
				map.get(p).add(oc);
			}

		}
		//prune unsupported patterns

		map.entrySet().removeIf(entry -> !patternFulfillsConstraints(entry.getKey(), entry.getValue()));
		return map;
	}
	
	/**
	 * Joins a generation of patterns according to definition 10.
	 * @param map - The current generation.
	 * @param k the generation number (length of patterns to be generated)
	 * @return Returns a map of all patterns that satisfy the minimum support. In addition all occurence series of each pattern are returned.
	 */
	protected Map<HybridTemporalPattern, List<Occurrence>> genLk(final Map<HybridTemporalPattern, List<Occurrence>> map, int k) throws InterruptedException {
		final Map<HybridTemporalPattern, List<Occurrence>> res = new ConcurrentHashMap<>();

		List<HybridTemporalPattern> list = new ArrayList<>(map.keySet());

		ExecutorService es = Executors.newFixedThreadPool(10);

		//AtomicInteger joined = new AtomicInteger(0);

		for(int i = 0; i < list.size(); i++) {
			final HybridTemporalPattern p1 = list.get(i);
			final List<Occurrence> l1 = map.get(p1);
			final HybridTemporalPattern prefix = ((DefaultHybridTemporalPattern)p1).getPrefix();
			for(int j = i; j < list.size(); j++) {
				final HybridTemporalPattern p2 = list.get(j);
				//only join patterns whose prefixes match
				if (((DefaultHybridTemporalPattern)p2).getPrefix() != prefix) {
					continue;
				}

				final List<Occurrence> l2 = map.get(p2);


				final Runnable join = () -> {
					res.putAll(HTPM.this.join(prefix, p1, l1, p2, l2, k));
					//System.out.println("joined " + joined.incrementAndGet() + " with " + l1.size() + " and " + l2.size() + " occurrences.");
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

		return res;
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
	 * @return Returns a map of patterns that satisfy the min-support and the desired length.
	 * For each pattern a complete list of its occurences will be returned. 
	 */
	protected Map<HybridTemporalPattern, List<Occurrence>> join(final HybridTemporalPattern prefix, final HybridTemporalPattern p1, final List<Occurrence> or1, final HybridTemporalPattern p2, final List<Occurrence> or2, int k) {
		final Map<HybridTemporalPattern, List<Occurrence>> map = new HashMap<>();

		for (int i1 = 0; i1 < or1.size(); i1++) {
			Occurrence s1 = or1.get(i1);
			//avoid join of same occurrences twice (happens if both are from the same occurrence record)
			int i2 = (or1 == or2 ? i1 + 1 : 0);
			for (; i2 < or2.size(); i2++) {
				Occurrence s2 = or2.get(i2);
				if (!occurrenceRecordsQualifyForJoin((DefaultOccurrence) s1, (DefaultOccurrence) s2)) {
					continue;
				}

				DefaultHybridTemporalPatternBuilder b = ORAlign(prefix, p1, s1, p2, s2, k);
				HybridTemporalPattern newPattern = b.getPattern();
				Occurrence newOccurrence = b.getOccurence();

				//prune new occurrence records
				if (newOccurrenceFulfillsConstraints(newPattern, newOccurrence, k)) {
					if (!map.containsKey(newPattern)) {
						map.put(newPattern, new ArrayList<>());
					}

					map.get(newPattern).add(newOccurrence);
				}
			}
		}

		//prune new patterns
		map.entrySet().removeIf(e -> !patternFulfillsConstraints(e.getKey(), e.getValue()));

		return map;
	}

	protected boolean occurrenceRecordsQualifyForJoin(DefaultOccurrence firstOccurrence, DefaultOccurrence secondOccurrence) {
		//make sure it is valid to merge the two occurrence records: only if they have same prefix (hence also from same sequence)
		return firstOccurrence.getPrefix() == secondOccurrence.getPrefix();
	}

	protected boolean newOccurrenceFulfillsConstraints(HybridTemporalPattern pattern, Occurrence occurrence, int k) {
		//patterns have correct length automatically. Further, each occurrence is generated only once.
		return true;
	}

	protected boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences) {
		//prune patterns which do not fulfill minimum support
		return this.isSupported(occurrences);
	}

	/**
	 * This method aligns two pattern according to "Example 7 (Joining two occurrence records)."
	 * 
	 * @param prefix - The shared prefix of the patterns.
	 * @param p1 - The first pattern.
	 * @param or1 - The occurence points of the first pattern.
	 * @param p2 - The second pattern.
	 * @param or2 - The occurence points of the second pattern.
	 * @param k
	 * @return Returns a map containing one pattern with one SeriesOccurence.
	 */
	protected DefaultHybridTemporalPatternBuilder ORAlign(final HybridTemporalPattern prefix,
														  final HybridTemporalPattern p1, final Occurrence or1,
														  final HybridTemporalPattern p2, final Occurrence or2, int k) {
		int i1 = 0;
		int i2 = 0;
		int ip = 0;

//		if(p1.toString().equals("htp1=(c)") && p2.toString().equals("htp1=(c)")) {
//			System.out.println("LETS DEBUG!!!");
//		}
		
		List<EventNode> pa1 = p1.getEventNodes();
		List<EventNode> pa2 = p2.getEventNodes();
		
		List<EventNode> pre;
		
		if(prefix != null) {
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
	 * @param a - The first EventNode
	 * @param oa - The occurence point of the first EventNode.
	 * @param b - The second EventNode
	 * @param ob - The occurence point of the second EventNode.
	 * @return <0 If a is before b, >0 if b is before a, 0 if equal.
	 */
	public static int compare(EventNode a, double oa, EventNode b, double ob) {

		//1 time
		int timeComparison = Double.compare(oa, ob);
		if (timeComparison != 0) {
			return timeComparison;
		}

		//remainder is about event nodes themselves
		return EventNode.compare(a, b);
	}

	/**
	 * Checks if a List of Occurrences has the minimum support
	 * @param occurrences the Occurrences to check
	 * @return true if minimum support is fulfilled, false otherwise
	 */
	protected boolean isSupported(final List<Occurrence> occurrences) {
		return !(this.support(occurrences) < this.min_sup);
	}

	/**
	 * Returns the support of a List of Occurrences
	 * @param occurrences the occurrences
	 * @return the support
	 */
	protected double support(final List<Occurrence> occurrences) {
		Set<String> sequenceIds = new HashSet<>();

		//support is implicitly counted by joining all joinable occurrence records once
		for(final Occurrence o : occurrences) {
			sequenceIds.add(o.getHybridEventSequence().getSequenceId());
		}

		return ((double) sequenceIds.size()) / ((double) this.d.size());
	}
}
