package de.dbvis.htpm;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.*;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
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

	/**
	 * Prefix tree of hybrid temporal patterns. Holds the child - canonical parent relations of patterns
	 * The canonical parent has the same events with the same order for the first length-1 events
	 */
	protected Map<HybridTemporalPattern, HybridTemporalPattern> patternPrefixTree = new ConcurrentHashMap<>();

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
				m = this.genLk(m);
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

			Occurrence emptyOccurrencePrefix = new DefaultOccurrence(seq);
			
			for(HybridEvent e : seq.getEvents()) {
				
				HybridTemporalPattern p = new DefaultHybridTemporalPattern("1", e);
				
				if(this.d.support(p) >= this.min_sup) {
					
					if(!map.containsKey(p)) {
						map.put(p, new ArrayList<>());
					}

					final Occurrence occurence = getOccurence(p, seq);
					map.get(p).add(occurence);
					//set empty occurrence as parent to be able to distinguish occurrences from different sequences
					//occurrencePrefixTree.put(occurence, emptyOccurrencePrefix);
					//this is a not-beautiful workaround for slow hashmap
					((DefaultOccurrence)occurence).setPrefix(emptyOccurrencePrefix);
				}
				
			}
			
		}
		
		return map;
	}
	
	/**
	 * Joins a generation of patterns according to definition 10.
	 * @param map - The current generation.
	 * @return Returns a map of all patterns that satisfy the minimum support. In addition all occurence series of each pattern are returned.
	 */
	protected Map<HybridTemporalPattern, List<Occurrence>> genLk(final Map<HybridTemporalPattern, List<Occurrence>> map) throws InterruptedException {
		final Map<HybridTemporalPattern, List<Occurrence>> res = new ConcurrentHashMap<>();

		List<HybridTemporalPattern> list = new ArrayList<>(map.keySet());

		ExecutorService es = Executors.newCachedThreadPool();
		
		for(int i = 0; i < list.size(); i++) {
			final HybridTemporalPattern p1 = list.get(i);
			final List<Occurrence> l1 = map.get(p1);
			final HybridTemporalPattern prefix = patternPrefixTree.get(p1);
			for(int j = i; j < list.size(); j++) {
				final HybridTemporalPattern p2 = list.get(j);
				//only join patterns whose prefixes match
				if (patternPrefixTree.get(p2) != prefix) {
					continue;
				}

				final List<Occurrence> l2 = map.get(p2);
				
				es.execute(() -> res.putAll(HTPM.this.join(prefix, p1, l1, p2, l2)));
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
	 * @return Returns a map of patterns that satisfy the min-support and the desired length.
	 * For each pattern a complete list of its occurences will be returned. 
	 */
	protected Map<HybridTemporalPattern, List<Occurrence>> join(final HybridTemporalPattern prefix, final HybridTemporalPattern p1, final List<Occurrence> or1, final HybridTemporalPattern p2, final List<Occurrence> or2) {
		final Map<HybridTemporalPattern, List<Occurrence>> map = new HashMap<>();

		for (int i1 = 0; i1 < or1.size(); i1++) {
			Occurrence s1 = or1.get(i1);
			Occurrence occPref = ((DefaultOccurrence) s1).getPrefix();
			int i2 = or1 == or2 ? i1 + 1 : 0;
			for (; i2 < or2.size(); i2++) {
				Occurrence s2 = or2.get(i2);
				//make sure it is valid to merge the two occurrence records: only if they have same prefix (hence also from same sequence)
				if (occPref != ((DefaultOccurrence) s2).getPrefix()) {
					continue;
				}
				Map<HybridTemporalPattern, Occurrence> m = ORAlign(prefix, p1, s1, p2, s2);

				//patterns have correct length automatically. Further, each occurrence is generated only once.
				m.forEach((p, o) -> {
					if (!map.containsKey(p)) {
						map.put(p, new ArrayList<>());
					}
					map.get(p).add(o);
				});
			}
		}

		return filterHybridTemporalPatterns(map);
	}

	protected Map<HybridTemporalPattern, List<Occurrence>> filterHybridTemporalPatterns(final Map<HybridTemporalPattern, List<Occurrence>> map) {
		//prune patterns which do not fulfill minimum support
		map.entrySet().removeIf(e -> !this.isSupported(e.getValue()));
		return map;
	}
	

	/**
	 * This method aligns two pattern according to "Example 7 (Joining two occurrence records)."
	 * 
	 * @param prefix - The shared prefix of the patterns.
	 * @param p1 - The first pattern.
	 * @param or1 - The occurence points of the first pattern.
	 * @param p2 - The second pattern.
	 * @param or2 - The occurence points of the second pattern.
	 * @return Returns a map containing one pattern with one SeriesOccurence.
	 */
	protected Map<HybridTemporalPattern, Occurrence> ORAlign(final HybridTemporalPattern prefix,
															 final HybridTemporalPattern p1, final Occurrence or1,
															 final HybridTemporalPattern p2, final Occurrence or2) {
		int i1 = 0;
		int i2 = 0;
		int ip = 0;

//		if(p1.toString().equals("htp1=(c)") && p2.toString().equals("htp1=(c)")) {
//			System.out.println("LETS DEBUG!!!");
//		}
		
		List<EventNode> pa1 = p1.getEventNodes();
		List<EventNode> pa2 = p2.getEventNodes();
		
		List<EventNode> pre = null;
		
		if(prefix != null) {
			pre = prefix.getEventNodes();
		}

		HTPBuilder b = new HTPBuilder(or1.getHybridEventSequence());

		HybridTemporalPattern patternPrefix = null;
		Occurrence occurrencePrefix = null;

		while (i1 < pa1.size() && i2 < pa2.size()) {
			final OccurrencePoint op1 = or1.get(i1);
			final OccurrencePoint op2 = or2.get(i2);
			double occurrence1 = op1.getTimePoint();
			double occurrence2 = op2.getTimePoint();
			final EventNode n1 = pa1.get(i1);
			final EventNode n2 = pa2.get(i2);
			final EventNode nP = pre != null && pre.size() > ip ? pre.get(ip) : null;

			if (n1.equals(nP) && n2.equals(nP)) {
				//both nodes are part of the "prefix", so it does not matter what we append
				b.append("p1", n1, op1);
				i1++;
				i2++;
				ip++;
			} else if (compare(n1, occurrence1, n2, occurrence2) < 0) {
				if (patternPrefix == null) {
					patternPrefix = p1;
					occurrencePrefix = or1;
				}
				b.append("p1", n1, op1);
				i1++;
			} else {
				if (patternPrefix == null) {
					patternPrefix = p2;
					occurrencePrefix = or2;
				}
				b.append("p2", n2, op2);
				i2++;
			}
		}

		if (i1 < pa1.size()) {
			do {
				b.append("p1", pa1.get(i1), or1.get(i1));
				i1++;
			} while (i1 < pa1.size());
		} else if (i2 < pa2.size()) {
			do {
				b.append("p2", pa2.get(i2), or2.get(i2));
				i2++;
			} while (i2 < pa2.size());
		}

		final HybridTemporalPattern newPattern = b.getPattern(p1.getPatternId());
		final Occurrence newOccurrence = b.getOccurences();

		patternPrefixTree.put(newPattern, patternPrefix);
		((DefaultOccurrence)newOccurrence).setPrefix(occurrencePrefix);

		HashMap<HybridTemporalPattern, Occurrence> result = new HashMap<>();
		result.put(newPattern, newOccurrence);
		return result;
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
	protected static int compare(EventNode a, double oa, EventNode b, double ob) {
		if(oa != ob) {
			if(oa > ob) {
				return 1;
			} else if(oa < ob) {
				return -1;
			}
			//return (int) Math.round(oa - ob);
		}
		
		if(!a.getEventNodeId().equals(b.getEventNodeId())) {
			return a.getEventNodeId().compareTo(b.getEventNodeId());
		}
		
		//3 types
		//a
		if(a instanceof IntervalStartEventNode && b instanceof IntervalEndEventNode) {
			return -1;
		}
		//a-reverse
		if(b instanceof IntervalStartEventNode && a instanceof IntervalEndEventNode) {
			return 1;
		}
		
		//b
		if(a instanceof IntervalStartEventNode && b instanceof PointEventNode) {
			return -1;
		}
		//b-reverse
		if(b instanceof IntervalStartEventNode && a instanceof PointEventNode) {
			return 1;
		}
		
		//c
		if(a instanceof PointEventNode && b instanceof IntervalEndEventNode) {
			return -1;
		}
		//c-reverse
		if(b instanceof PointEventNode && a instanceof IntervalEndEventNode) {
			return 1;
		}
		
		//4 occurencemark
		if(a instanceof IntervalEventNode && b instanceof IntervalEventNode) {
			return ((IntervalEventNode) a).getOccurrenceMark() - ((IntervalEventNode) b).getOccurrenceMark();
		}
		
		//tie
		return 0;
	}
	
	/**
	 * Generates the occurence of a pattern in a EventSequence.
	 * @param p - The pattern.
	 * @param seq - The sequence.
	 * @return - A GeneralSeriesOccurence-Object that holds all the occurence points of the pattern.
	 */
	protected Occurrence getOccurence(HybridTemporalPattern p, HybridEventSequence seq) {
		DefaultOccurrence oc = new DefaultOccurrence(seq);
		for(HTPItem i : p.getPatternItems()) {
			if(i instanceof EventNode) {
				oc.add((EventNode) i);
			}
		}
		return oc;
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
	
	/**
	 * A helper class that appends EventNodes and their occurrences in order to build a pattern.
	 * 
	 * @author Wolfgang Jentner
	 *
	 */
	protected class HTPBuilder {

		protected List<EventNode> ev;
		protected Map<String, Integer> occurrencemarks;
		protected Map<String, Integer> occurrencemark_of_startinterval;
		protected DefaultOccurrence go;
		
		public HTPBuilder(HybridEventSequence seq) {
			this.ev = new ArrayList<>();
			this.occurrencemarks = new HashMap<>();
			this.occurrencemark_of_startinterval = new HashMap<>();
			this.go = new DefaultOccurrence(seq);
		}
		
		protected String generateKeyForOccurrenceMarks(String frompattern, String eventid, int occurrencemark) {
			return this.generateKeyForOpenIntervals(frompattern, eventid)+":"+occurrencemark;
		}
		
		protected String generateKeyForOpenIntervals(String frompattern, String eventid) {
			return frompattern+":"+eventid;
		}
		
		/**
		 * This method appends an EventNode to a list. It takes care of start and end nodes.
		 * @param e - The EventNode to add.
		 * @param op - The occurence point of the event node.
		 */
		public void append(String frompattern, EventNode e, OccurrencePoint op) {
			if(e instanceof PointEventNode) {
				ev.add(new PointEventNode(e.getEventNodeId(), op.getTimePoint()));
				go.add(op);
			} else if(e instanceof IntervalStartEventNode) {
				int occurrencemark = (this.occurrencemarks.containsKey(e.getEventNodeId())) ? this.occurrencemarks.get(e.getEventNodeId()) + 1 : 0;
				this.occurrencemarks.put(e.getEventNodeId(), occurrencemark); //update
				
				String key = this.generateKeyForOccurrenceMarks(frompattern, e.getEventNodeId(), ((IntervalEventNode) e).getOccurrenceMark());
				this.occurrencemark_of_startinterval.put(key, occurrencemark);
				//this.openintervals.add(this.generateKeyForOpenIntervals(frompattern, e.getId()));
				
				ev.add(new IntervalStartEventNode(e.getEventNodeId(), op.getTimePoint(), occurrencemark));
				go.add(op);
			} else if(e instanceof IntervalEndEventNode) {
				IntervalEndEventNode ie = (IntervalEndEventNode) e;
				String key = this.generateKeyForOccurrenceMarks(frompattern, ie.getEventNodeId(), ie.getOccurrenceMark());
				int occurrencemark;
				if(this.occurrencemark_of_startinterval.containsKey(key)) {
					occurrencemark = this.occurrencemark_of_startinterval.get(key);
				} else {
					throw new RuntimeException("Could not find corresponding IntervalStartEventNode for key " + key);
				}
				
				ev.add(new IntervalEndEventNode(ie.getEventNodeId(), op.getTimePoint(), occurrencemark));
				go.add(op);
			} else {
				throw new UnsupportedOperationException("Unknown EventNode type");
			}
		}
		
		public HybridTemporalPattern getPattern(String id) {
			EventNode[] evs = new EventNode[ev.size()];
			evs = this.ev.toArray(evs);
			HybridTemporalPattern p = new DefaultHybridTemporalPattern(id, evs);
			if(!p.isValid()) {
				throw new RuntimeException("invalid pattern after join " + p);
			}
			return p;
		}
		
		public Occurrence getOccurences() {
			return this.go;
		}
		
	}

}
