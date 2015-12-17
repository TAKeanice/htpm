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
			
			for(HybridEvent e : seq.getEvents()) {
				
				HybridTemporalPattern p = new DefaultHybridTemporalPattern("1", e);
				
				if(this.d.support(p) >= this.min_sup) {
					
					if(!map.containsKey(p)) {
						map.put(p, new ArrayList<>());
					}
					
					map.get(p).add(getOccurence(p, seq));
					
				}
				
			}
			
		}
		
		return map;
	}
	
//	/**
//	 * Tests wether two patterns share one common prefix.
//	 * @param p1 - Pattern 1
//	 * @param p2 - Pattern 2
//	 * @return True if the two patterns share a common prefix, false otherwise.
//	 */
//	protected boolean shareCommonPrefix(HybridTemporalPattern p1, HybridTemporalPattern p2) {
//		return p1.deleteLastEvent().equals(p2.deleteLastEvent());
//	}
	
	/**
	 * Joins a generation of patterns according to definition 10.
	 * @param map - The current generation.
	 * @param k - The length of the desired generation. Typically +1 of current.
	 * @return Returns a map of all patterns that satisfy the minimum support. In addition all occurence series of each pattern are returned.
	 */
	protected Map<HybridTemporalPattern, List<Occurrence>> genLk(final Map<HybridTemporalPattern, List<Occurrence>> map, final int k) throws InterruptedException {
		final Map<HybridTemporalPattern, List<Occurrence>> res = new ConcurrentHashMap<>();
		
		List<HybridTemporalPattern> list = new ArrayList<>(map.keySet());

		ExecutorService es = Executors.newCachedThreadPool();
		
		for(int i = 0; i < list.size(); i++) {
			final HybridTemporalPattern p1 = list.get(i);
			final List<Occurrence> l1 = map.get(p1);
			final HybridTemporalPattern prefix = p1.deleteLastEvent();
			for(int j = 0; j <= i; j++) {
				final HybridTemporalPattern p2 = list.get(j);
				final List<Occurrence> l2 = map.get(p2);


				es.execute(() -> res.putAll(HTPM.this.join(prefix, p1, l1, p2, l2, k)));

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
	 * @param k - The desired length of the resulting pattern. See HTP.length for more information.
	 * @return Returns a map of patterns that satisfy the min-support and the desired length.
	 * For each pattern a complete list of its occurences will be returned. 
	 */
	protected Map<HybridTemporalPattern, List<Occurrence>> join(final HybridTemporalPattern prefix, final HybridTemporalPattern p1, final List<Occurrence> or1, final HybridTemporalPattern p2, final List<Occurrence> or2, final int k) {
		final Map<HybridTemporalPattern, List<Occurrence>> map = new HashMap<>();

		for (final Occurrence s1 : or1) {
			for (final Occurrence s2 : or2) {
				if (!s1.getHybridEventSequence().getSequenceId().equals(s2.getHybridEventSequence().getSequenceId())) {
					continue;
				}

				final Map<HybridTemporalPattern, Occurrence> m = ORAlign(prefix, p1, s1, p2, s2);

				m.entrySet()
						.stream()
						.filter(e -> e.getKey().length() == k)
						.forEach(e -> {

							final HybridTemporalPattern p = e.getKey();
							final Occurrence o = e.getValue();

							if (!map.containsKey(p)) {
								map.put(p, new LinkedList<>());
							}

							if (!map.get(p).contains(o)) {
								map.get(p).add(o);
							}
						});
			}
		}

		return filterHybridTemporalPatterns(map);
	}

	protected Map<HybridTemporalPattern, List<Occurrence>> filterHybridTemporalPatterns(final Map<HybridTemporalPattern, List<Occurrence>> map) {
		//prune patterns which do not fulfill minimum support
		Iterator<Map.Entry<HybridTemporalPattern, List<Occurrence>>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<HybridTemporalPattern, List<Occurrence>> e = iterator.next();
			if(!this.isSupported(e.getValue())) {
				iterator.remove();
			}
		}
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
	protected Map<HybridTemporalPattern, Occurrence> ORAlign(final HybridTemporalPattern prefix, final HybridTemporalPattern p1, final Occurrence or1, final HybridTemporalPattern p2, final Occurrence or2) {
		int ia = 0;
		int ib = 0;

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
		
		while(ia < pa1.size() && ib < pa2.size()) {
			final OccurrencePoint op1 = or1.get(ia);
			final OccurrencePoint op2 = or2.get(ib);
			double occurrence1 = op1.getTimePoint();
			double occurrence2 = op2.getTimePoint();
			final EventNode n1 = pa1.get(ia);
			final EventNode n2 = pa2.get(ib);
			
			if(occurrence1 == occurrence2
					&& (
							(n1.isStartEvent() && n2.isStartEvent())
						 || (n1.isEndEvent() && n2.isEndEvent())
						 || (n1.isPointEvent() && n2.isPointEvent())
						)
				) { //Case 1
				
				if(isPrefix(pre, n1) && isPrefix(pre, n2)) { //both are prefixes
					b.append("p1", n1, op1);
					ia++;
					ib++;
				} else if(compare(n1, occurrence1, n2, occurrence2) < 0) {
					b.append("p1", n1, op1);
					ia++;
				} else {
					b.append("p2", n2, op2);
					ib++;
				}
				
			} else if(occurrence1 < occurrence2) { //Case 2
				b.append("p1", n1, op1);
				ia++;
			} else { //Case 3
				b.append("p2", n2, op2);
				ib++;
			}
		}
			
		while(ia < pa1.size()) {
			b.append("p1", pa1.get(ia), or1.get(ia));
			ia++;
		}
		
		while(ib < pa2.size()) {
			b.append("p2", pa2.get(ib), or2.get(ib));
			ib++;
		}

		//This or-align method has a flaw as it merges events from the same Occurrence:
		//Imagine two patterns htp1=(c) at seq1=(6.0) and another one at htp1=(c) at seq1=(6.0)
		//this means c occurs at seq1 at timepoint 6.0
		//With this method the resulting pattern will be
		//htp1=(c=c) at (6.0,6.0)
		//This is not true since we don't have it in the occurrences

		//for now this is covered in the HybridEventSequence.occur(Occurrence) method
		//this one will return false and so the pattern won't be considered into the next generation

		Map<HybridTemporalPattern, Occurrence> m = new HashMap<>();

		//still... it would be a lot better to not have such weird results out of this algorithm
		//in general
		if(!b.getOccurences().getHybridEventSequence().isValid(b.getOccurences())) {
			return m; //we return an empty map
		}

		m.put(b.getPattern(p1.getPatternId()), b.getOccurences());
		return m;
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
	 * This method checks if a HTPItem is part of the prefix or not.
	 * @param prefix - The prefix
	 * @param item - The item to check
	 * @return true if the item is part of the prefix, false otherwise.
	 */
	protected static boolean isPrefix(List<EventNode> prefix, HTPItem item) {
		if(prefix == null) {
			return false;
		}
		for(EventNode e : prefix) {
			if(e.equals(item)) {
				return true;
			}
		}
		return false;
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

		for(final Occurrence o : occurrences) {
			if(!o.getHybridEventSequence().isValid(o)) {
				throw new RuntimeException("Uhoh this should not happen at all");
			}

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
					throw new RuntimeException("Could not find corresponding IntervalStartEventNode for key "+key);
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
