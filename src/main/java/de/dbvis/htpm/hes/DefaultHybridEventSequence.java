package de.dbvis.htpm.hes;

import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.*;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.DefaultOccurrencePoint;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.occurrence.OccurrencePoint;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder.buildFromHybridEventList;

/**
 * This class implements the HybridEventSequence.
 * 
 * @author Andreas Stoffel, Wolfgang Jentner
 *
 */
public class DefaultHybridEventSequence implements HybridEventSequence {

	// Thank goes to Andreas Stoffel (Data Analysis & Visualization Group,
	// University of Konstanz) for the help with the occur() methods.
	/**
	 * A list holding all the HybridEvents
	 */
	protected List<HybridEvent> events;
	
	/**
	 * The id of the HybridEventSequence
	 */
	protected String sequenceId;
	
	//stored for performance reasons
	/**
	 * a set storing all the ids of the HybridEvents,
	 * this is used as a heuristic by the occur(...) methods
	 * to speed them up
	 */
	private Set<String> eventids;

	protected enum EventType {
		start,
		end,
		point
	}

	/**
	 * This hashmap points from the timepoint, to the eventid, to all HybridEvents that occur there
	 */
	private Map<Double, Map<EventType, List<HybridEvent>>> occurrenceIndex = new HashMap<>();

	private Map<MyItem, Integer> myIndex = new HashMap<>();

	private Set<HybridEvent> containedEvents = new HashSet<>();
	//a list since an event can occur multiple times at the same time
	
	/**
	 * Creates a new, empty HybridEventSequence with a given id.
	 * @param sequenceId the id of the HybridEventSequence; must not be null
	 */
	public DefaultHybridEventSequence(String sequenceId) {
		if(sequenceId == null) {
			throw new NullPointerException("The HybridEventSequence id must not be null");
		}
		this.sequenceId = sequenceId;
		this.events = new ArrayList<>();
		this.eventids = new HashSet<>();
	}

	public DefaultHybridEventSequence(String sequenceId, List<HybridEvent> events) {
		this(sequenceId);
		for (HybridEvent event : events) {
			this.add(event);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder(this.getSequenceId() + "=");
		for(int i = 0; i < events.size(); i++) {
			s.append(events.get(i));
			if(i < (events.size() -1)) {
				s.append(";");
			}
		}
		return s.toString();
	}
	
	@Override
	public String getSequenceId() {
		return this.sequenceId;
	}
	
	@Override
	public void add(HybridEvent e) {
		if(e != null) {
			this.events.add(e);
			this.eventids.add(e.getEventId());

			//build up index structure:
			//addToIndex(e);

			this.containedEvents.add(e);
		}
	}

	public void addToIndex(HybridEvent e) {
		if(e.isPointEvent()) {
			this.addHybridEventToIndex(e.getTimePoint(), EventType.point, e);

			this.addToIndex(this.myIndex, new MyItem(e, e.getTimePoint()));

		} else {
			this.addHybridEventToIndex(e.getStartPoint(), EventType.start, e);
			this.addHybridEventToIndex(e.getEndPoint(), EventType.end, e);

			this.addToIndex(this.myIndex, new MyItem(e, e.getStartPoint()));
			this.addToIndex(this.myIndex, new MyItem(e, e.getEndPoint()));
		}
	}

	protected void addToIndex(final Map<MyItem, Integer> index, final MyItem item) {
		if(!index.containsKey(item)) {
			index.put(item, 0);
		}
		index.put(item, index.get(item) + 1);
	}
	
	@Override
	public void remove(HybridEvent e) {
		this.events.remove(e);
		boolean contains = false;
		for(HybridEvent e2 : events) {
			if(e2.getEventId().equals(e.getEventId())) {
				contains = true;
				break;
			}
		}
		if(!contains) {
			this.eventids.remove(e.getEventId());
		}
	}
	
	@Override
	public List<HybridEvent> getEvents() {
		return new ArrayList<>(this.events);
	}
	
	//def 4
	@Override
	public List<Occurrence> occur(String id) {
		List<Occurrence> res = new ArrayList<>();
		//occurrence marks can be only 0 since it will be the
		//only node in that occurrence
		events.stream().filter(e -> e.getEventId().equals(id)).forEach(e -> {

			List<OccurrencePoint> ops;
			if (e.isPointEvent()) {
				ops = Collections.singletonList(new DefaultOccurrencePoint(e));
			} else {
				//occurrence marks can be only 0 since it will be the
				//only node in that occurrence
				ops = Arrays.asList(
						new DefaultOccurrencePoint(e, true),
						new DefaultOccurrencePoint(e, false));
			}

			DefaultOccurrence oc = new DefaultOccurrence(this, ops);
			res.add(oc);
		});
		return Collections.unmodifiableList(res);
	}
	
	//enhanced def 4
	@Override
	public List<Occurrence> occur(HybridEvent e) {
		return this.occur(e.getEventId());
	}
	
	@Override
	public List<Occurrence> occur(HybridTemporalPattern p) {
		return this.occur2(p, false);
	}
	
	@Override
	public boolean supports(HybridTemporalPattern p) {
		return !this.occur2(p, true).isEmpty(); 
	}

	@Override
	public boolean isValid(final Occurrence o) {
		if(o.getHybridEventSequence() != this) {
			throw new RuntimeException("Ooops something went terribly wrong here");
		}

		//since the HybridEvent equals with
		final Map<MyItem, Integer> seenItems = new HashMap<>();

		for(OccurrencePoint op : o.ops()) {
			if(!occurs(op, seenItems)) {
				return false;
			}
		}

		return true;
	}

	protected boolean occurs(OccurrencePoint op, final Map<MyItem, Integer> seenEvents) {
		if(op.getHybridEvent() == null) {
			throw new IllegalArgumentException("OccurrencePoint must contain HybridEvent");
		}

		final HybridEvent toSearchFor = op.getHybridEvent();

		if(!this.containedEvents.contains(toSearchFor)) {
			return false;
		}

		MyItem searchItem = new MyItem(toSearchFor, op.getTimePoint());

		if(!this.myIndex.containsKey(searchItem)) {
			return false;
		}

		this.addToIndex(seenEvents, searchItem);

		return seenEvents.get(searchItem) <= this.myIndex.get(searchItem);

	}

	protected void addHybridEventToIndex(final double timepoint, final EventType type, final HybridEvent e) {
		if(!this.occurrenceIndex.containsKey(timepoint)) {
			this.occurrenceIndex.put(timepoint, new HashMap<>());
		}

		if(!this.occurrenceIndex.get(timepoint).containsKey(type)) {
			this.occurrenceIndex.get(timepoint).put(type, new LinkedList<>());
		}

		this.occurrenceIndex.get(timepoint).get(type).add(e);
	}

	/**
	 * The internal implementation of the occur(HybridTemporPattern) method.
	 * Is speeded up by using some simple heuristics.
	 * @param p the HybridTemporalPattern to check with
	 * @param findfirst if true it only returns the first found occurrence (used by the support method in order to speed it up)
	 * @return A collection of Occurrences
	 */
	protected List<Occurrence> occur2(HybridTemporalPattern p, boolean findfirst) {
		//heur1: check length
		if(p.length() > this.events.size()) {
			return Collections.emptyList();
		}
		
		//heur2: check if an event id of p is not in seq
		for(String eid : p.getEventIds()) {
			if(!this.eventids.contains(eid)) {
				return Collections.emptyList();
			}
		}
		
		List<HybridEvent> pevents = new ArrayList<>();
		for(String eid : p.getEventIds()) {
			pevents.addAll(this.events.stream().filter(e -> eid.equals(e.getEventId())).collect(Collectors.toList()));
		}
		
		//transform both, the pattern and this sequence into a
		//list of HTPItems to find occurrences
		List<HTPItem> own = buildFromHybridEventList(this, pevents).getPattern().getPatternItemsInIntegerIdOrder();
		List<HTPItem> ext = p.getPatternItemsInIntegerIdOrder();
		
		//return occur2impl(own, ext, findfirst);
		return Collections.emptyList();
	}
	
	/** TODO: this is currently not working. fix or remove.
	 * Finds occurrences based on the HTPItem-Lists.
	 * @param own the own HTPItems
	 * @param pattern the HTPItems of the pattern
	 * @param findfirst if true, abort search after we found the first occurrence (speedup)
	 * @return A list of occurrences
	 */
	/*protected List<Occurrence> occur2impl(List<HTPItem> own, List<HTPItem> pattern, boolean findfirst) {
		List<Occurrence> results = new ArrayList<>();
		List<List<HTPItem>> partials = new CopyOnWriteArrayList<>();
		for (HTPItem item : own) {
			for (List<HTPItem> partial : partials) {
				if (isRequiredEndEvent(partial, item)) {
					partials.remove(partial);
				}
				if (!isAppendable(partial, item)) continue;

				if (!isPartialMatch(partial, item, pattern)) {
					continue;
				} 
				List<HTPItem> child = new ArrayList<>(partial.size() + 1);
				child.addAll(partial);
				child.add(item);
				if (child.size() == pattern.size()) {
					DefaultOccurrence oc = new DefaultOccurrence(this);
					child.stream().filter(part -> part instanceof EventNode).forEach(part -> oc.add(
							new DefaultOccurrencePoint(((EventNode) part).getHybridEvent(),
									part instanceof IntervalStartEventNode)));
					results.add(oc);
					if(findfirst) {
						return results;
					}
				} else {
					if (item instanceof OrderRelation) {
						partials.remove(partial);
					}
					partials.add(child);
				}
			}
			if (isStart(item, pattern)) {
				if (pattern.size() == 1) {
					//GeneralOccurrence resultOccurence = new GeneralOccurrence();
					DefaultOccurrence oc = new DefaultOccurrence(this);
					if (item instanceof EventNode) {
						//resultOccurence.add(((EventNode) item).getTimePoint(), ((EventNode) item).getMetadata());
						oc.add(new DefaultOccurrencePoint(((EventNode) item).getHybridEvent(),
								item instanceof IntervalStartEventNode));
					}
					//results.add(resultOccurence);
					results.add(oc);
					if(findfirst) {
						return results;
					}
				} else {
					partials.add(Collections.singletonList(item));
				}
			}
		}
		return results;
	}*/
	
	/**
	 * Checks if a given HTPItem (end event) has a corresponding start event in the partial list.
	 * @param partial the partial list
	 * @param item the end event
	 * @return true if the corresponding start event can be found, false otherwise
	 */
	protected boolean isRequiredEndEvent(List<HTPItem> partial, HTPItem item) {
		if (!(item instanceof IntervalEndEventNode)) return false;
		IntervalEndEventNode itemEvent = (IntervalEndEventNode) item;
		for (HTPItem p : partial) {
			if (p instanceof IntervalStartEventNode) {
				IntervalStartEventNode pEvent = (IntervalStartEventNode) p;
				if (pEvent.getStringEventId().equals(itemEvent.getStringEventId()) && pEvent.getOccurrenceMark() == itemEvent.getOccurrenceMark()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the given sequence is appendable by a given HTPItem.
	 * This is true if either the last event is an order relation (/event node) and the item is a event node (/order relation)
	 * or the sequence is empty
	 * @param sequence the sequence
	 * @param item the item to append
	 * @return true if it satisfies the constraints defined above, false otherwise
	 */
	protected boolean isAppendable(List<HTPItem> sequence, HTPItem item) {
		assert (sequence != null);
		assert (!sequence.isEmpty());
		HTPItem last = sequence.get(sequence.size() - 1);
		return (last instanceof OrderRelation && item instanceof EventNode)
				|| (last instanceof EventNode && item instanceof OrderRelation);
	}
	
	/**
	 * Checks if a given HTPItem is the start of a given pattern
	 * @param item the HTPItem
	 * @param pattern the pattern
	 * @return true if it is the start, false otherwise
	 */
	protected boolean isStart(HTPItem item, List<HTPItem> pattern) {
		HTPItem head = pattern.get(0);
		if (!item.getClass().equals(head.getClass())) return false;
		if (item instanceof OrderRelation) {
			return item.equals(head);
		} else if (item instanceof EventNode) {
			return ((EventNode) item).getStringEventId().equals(((EventNode) head).getStringEventId());
		} else {
			throw new IllegalStateException("Unknown item type " + item.getClass().getName());
		}
	}
	
	/**
	 * Checks if given list of items is a partial match in the pattern.
	 * If yes it does the same for the given item.
	 * @param items the List of HTPItems
	 * @param item the HTPItem
	 * @param pattern the pattern
	 * @return true if it is a partial match, false otherwise
	 */
	protected boolean isPartialMatch(List<HTPItem> items, HTPItem item, List<HTPItem> pattern) {
		if (items.size() >= pattern.size()) return false;
		Map<String, Integer> occurenceMap = new HashMap<>();
		for (int i = 0; i < items.size(); ++i) {
			HTPItem item2 = items.get(i);
			HTPItem pat = pattern.get(i);
			if(!isMatching(occurenceMap, item2, pat)) {
				return false;
			}
		}
		return isMatching(occurenceMap, item, pattern.get(items.size()));
	}
	
	/**
	 * A helper method of the isPartialMatch method.
	 * Checks if a item is matching another item from the pattern.
	 * @param occurenceMap a memory for the interval events, to find either the corresponding start/end interval event
	 * @param item the item to check
	 * @param pat the item from the pattern to check against
	 * @return true if it matches, false otherwise
	 */
	protected boolean isMatching(Map<String, Integer> occurenceMap, HTPItem item, HTPItem pat) {
		if (!item.getClass().equals(pat.getClass())) return false;
		if (item instanceof OrderRelation) {
			return item.equals(pat);
		} else if (item instanceof PointEventNode) {
			return ((PointEventNode) item).getStringEventId().equals(((PointEventNode) pat).getStringEventId());
		} else if (item instanceof IntervalStartEventNode) {
			IntervalStartEventNode itemNode = (IntervalStartEventNode) item;
			IntervalStartEventNode patNode = (IntervalStartEventNode) pat;
			if (!itemNode.getStringEventId().equals(patNode.getStringEventId())) return false;
			occurenceMap.put(patNode.getStringEventId() + "+" + patNode.getOccurrenceMark(), itemNode.getOccurrenceMark());
		} else {
			if (item instanceof IntervalEndEventNode) {
				IntervalEndEventNode itemNode = (IntervalEndEventNode) item;
				IntervalEndEventNode patNode = (IntervalEndEventNode) pat;
				if (!itemNode.getStringEventId().equals(patNode.getStringEventId())) return false;
				Integer occurenceMark = occurenceMap.get(patNode.getStringEventId() + "+" + patNode.getOccurrenceMark());
				if (occurenceMark == null)
					throw new IllegalArgumentException("End event " + pat + " before start event.");
				return itemNode.getOccurrenceMark() == occurenceMark;
			} else {
				throw new IllegalStateException("Unknown item type " + item.getClass().getName());
			}
		}
		return true;
	}

	protected static class MyItem {
		private EventType type;
		final private HybridEvent ev;
		final double timePoint;

		public MyItem(final HybridEvent ev, final double timepoint) {
			this.ev = ev;
			this.timePoint = timepoint;
			if(ev.isPointEvent()) {
				this.type = EventType.point;
			} else if(!ev.isPointEvent()) {
				if(ev.getStartPoint() == timepoint) {
					this.type = EventType.start;
				} else if(ev.getEndPoint() == timepoint) {
					this.type = EventType.end;
				}
			} else {
				throw new RuntimeException("Could not determine type");
			}
		}

		public EventType getType() {
			return type;
		}

		public HybridEvent getHybridEvent() {
			return ev;
		}

		public double getTimePoint() {
			return timePoint;
		}

		public boolean equals(Object o) {
			if(!(o instanceof MyItem)) {
				return false;
			}
			if(o == this) {
				return true;
			}

			MyItem i = (MyItem) o;
			return new EqualsBuilder()
					.append(this.getType(), i.getType())
					.append(this.getTimePoint(), i.getTimePoint())
					.append(this.getHybridEvent(), i.getHybridEvent())
					.isEquals();
		}

		public int hashCode() {
			return new HashCodeBuilder(13, 17)
					.append(this.getType())
					.append(this.getTimePoint())
					.append(this.getHybridEvent())
					.hashCode();
		}
	}
}
