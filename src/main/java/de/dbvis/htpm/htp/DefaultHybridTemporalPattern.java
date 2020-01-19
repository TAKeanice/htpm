package de.dbvis.htpm.htp;

import de.dbvis.htpm.htp.eventnodes.*;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultHybridTemporalPattern implements HybridTemporalPattern {

	private final EventNode[] eventnodes;
	private final OrderRelation[] orderrelations;

	//stored for performance reasons
	private Integer length = null;
	private String patternstr = null;
	private Integer hashcode = null;
	private List<String> eventids = null;
	private Boolean isValid = null;

	/**
	 * Method to create default pattern from already accumulated event nodes and order relations
	 * No further checks are performed, so the nodes and relations have to build a valid pattern.
	 * @param eventnodes the event nodes of the pattern, in integer id order
	 * @param orderrelations the order relations of the pattern
	 */
	public DefaultHybridTemporalPattern(List<EventNode> eventnodes, List<OrderRelation> orderrelations) {
		this.eventnodes = eventnodes.toArray(new EventNode[0]);
		this.orderrelations = orderrelations.toArray(new OrderRelation[0]);
	}

	/**
	 * Method to create pattern from string. See original paper for correct specification
	 * Additionally to the original specification, elements of a pattern can be quoted in double quotes,
	 * and between a pair of double quotes all special symbols (+,-,<,=) are allowed.
	 * @param pattern the pattern string. Example: a+0=b<a-0<c (round brackets around the pattern are allowed)
	 */
	public DefaultHybridTemporalPattern(String pattern) {

		if (pattern.matches("\\(.*\\)")) {
			pattern = pattern.substring(1, pattern.length() - 1);
		}

		if (pattern.isEmpty()) {
			eventnodes = new EventNode[0];
			orderrelations = new OrderRelation[0];
			return;
		}

		List<EventNode> eventnodes = new ArrayList<>();
		List<OrderRelation> orderrelations = new ArrayList<>();
		try {
			parsePatternString(pattern, eventnodes, orderrelations);
		} catch (Exception e) {
			throw new IllegalArgumentException("Pattern string " + pattern + " could not be parsed", e);
		}

		this.eventnodes = eventnodes.toArray(new EventNode[0]);
		this.orderrelations = orderrelations.toArray(new OrderRelation[0]);
	}

	private void parsePatternString(String pattern, List<EventNode> eventnodes, List<OrderRelation> orderrelations) throws ParseException {
		//this is a hack, adding the = in the end of the pattern makes the pattern
		//iteself invalid, but the loop will continue once more and also adds
		//the last EventNode
		char[] p = (pattern + "=").toCharArray();

		StringBuilder id = new StringBuilder(100);
		StringBuilder oc = new StringBuilder(100);
		OrderRelation lastRel = null;
		boolean parsingOccurencemark = false;
		boolean insideQuotes = false;
		boolean isStartEvent = false;
		for(char c : p) {

			if (c == '"') {
				insideQuotes = !insideQuotes;
				id.append(c);
				continue;
			}

			if (insideQuotes) {
				id.append(c);
				continue;
			}

			if(!parsingOccurencemark && (c == '<' || c == '=')) {
				//is point event
				if (lastRel != null) {
					orderrelations.add(lastRel);
				}
				eventnodes.add(new PointEventNode(id.toString()));
				id = new StringBuilder(100);
				lastRel = OrderRelation.fromChar(c);
				continue;
			}

			if(c == '+') {
				parsingOccurencemark = true;
				isStartEvent = true;
				continue;
			}

			if(c == '-') {
				parsingOccurencemark = true;
				isStartEvent = false;
				continue;
			}

			if(parsingOccurencemark && (c == '<' || c == '=')) {
				//we have parsed an intervalevent
				if (lastRel != null) {
					orderrelations.add(lastRel);
				}
				if(isStartEvent) {
					eventnodes.add(new IntervalStartEventNode(id.toString(), Integer.parseInt(oc.toString())));
				} else {
					eventnodes.add(new IntervalEndEventNode(id.toString(), Integer.parseInt(oc.toString())));
				}

				id = new StringBuilder(100);
				oc = new StringBuilder(100);
				isStartEvent = false;
				parsingOccurencemark = false;
				lastRel = OrderRelation.fromChar(c);
				continue;
			}

			if(!parsingOccurencemark) {
				id.append(c);
			} else {
				oc.append(c);
			}
		}

		if (insideQuotes) {
			throw new ParseException("Quotes are not balanced!", pattern.length());
		}

		Comparator<EventNode> c = EventNode::compareByIntId;
		HTPUtils.sortItemsets(eventnodes, orderrelations, c);
	}

	public String toString() {
		return this.patternStr();
	}

	@Override
	public String patternStr() {
		if (patternstr == null) {
			patternstr = "(";
			int[] allIndices = IntStream.range(0, eventnodes.length).toArray();
			patternstr += partialPatternStr(allIndices);
			patternstr += ")";
		}
		return this.patternstr;
	}

	@Override
	public String partialPatternStr(int... indices) {

		if (indices.length == 0) {
			return "";
		}

		List<EventNode> nodes = Arrays.stream(indices).mapToObj(i -> eventnodes[i]).collect(Collectors.toList());

		List<OrderRelation> relations = new ArrayList<>(nodes.size() - 1);
		for (int i = 1; i < indices.length; i++) {
			relations.add(small(indices[i-1], indices[i]));
		}

		HTPUtils.sortItemsets(nodes, relations, EventNode::compareByStringId);

		StringBuilder str = new StringBuilder();
		for (int i = 0; i < relations.size(); i++) {
			str.append(nodes.get(i));
			str.append(relations.get(i));
		}
		str.append(nodes.get(nodes.size() - 1));

		return str.toString();
	}

	@Override
	public int size() {
		return eventnodes.length;
	}

	@Override
	public EventNode getEventNode(int i) {
		return eventnodes[i];
	}

	@Override
	public List<EventNode> getEventNodes() {
		return Arrays.asList(eventnodes);
	}

	@Override
	public List<OrderRelation> getOrderRelations() {
		return Arrays.asList(orderrelations);
	}

	/**
	 * The length is defined by the number of events in the pattern.
	 * Start and end of an interval count as one.
	 * @return number of events
	 */
	@Override
	public int length() {
		if (length == null) {
			this.length = 0;
			for (int i = 0; i < eventnodes.length; i++) {
				EventNode node = this.eventnodes[i];
				if (node instanceof PointEventNode || node instanceof IntervalStartEventNode) {
					this.length++;
				}
			}
		}
		return this.length;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o == this) {
			return true;
		} else if (o instanceof DefaultHybridTemporalPattern) {
			DefaultHybridTemporalPattern other = (DefaultHybridTemporalPattern) o;
			//we know that the other pattern is defined by its two arrays, just like this pattern
			//thus we can directly compare them.
			return Arrays.equals(this.eventnodes, other.eventnodes)
					&& Arrays.equals(this.orderrelations, other.orderrelations);
		} else if(o instanceof HybridTemporalPattern) {
			//resort to comparison of pattern elements, since we do not know the other´s internal structure
			return HTPUtils.equal(this, (HybridTemporalPattern) o);
		}
		return false;
	}

	@Override
	public int hashCode() {
		//build hashcode from all htpitems
		if (hashcode == null) {
			this.hashcode = HTPUtils.hashCode(this);
		}
		return this.hashcode;
	}

	@Override
	public int compareTo(HybridTemporalPattern o) {
		return HTPUtils.compare(this, o);
	}

	@Override
	public List<HTPItem> getPatternItemsInIntegerIdOrder() {
		if (eventnodes == null || eventnodes.length == 0) {
			return Collections.emptyList();
		}
		List<HTPItem> patternItems = new ArrayList<>(this.orderrelations.length * 2 + 1);
		for (int i = 0; i < orderrelations.length; i++) {
			patternItems.add(eventnodes[i]);
			patternItems.add(orderrelations[i]);
		}
		patternItems.add(eventnodes[eventnodes.length - 1]);
		return patternItems;
	}

	@Override
	public List<HTPItem> getPatternItemsInStringIdOrder() {
		if (eventnodes == null || eventnodes.length == 0) {
			return Collections.emptyList();
		}
		List<EventNode> nodes = Arrays.asList(eventnodes);
		List<OrderRelation> relations = Arrays.asList(orderrelations);
		HTPUtils.sortItemsets(nodes, relations, EventNode::compareByStringId);
		List<HTPItem> items = new ArrayList<>(relations.size() * 2 + 1);
		for (int i = 0; i < orderrelations.length; i++) {
			items.add(nodes.get(i));
			items.add(relations.get(i));
		}
		items.add(eventnodes[eventnodes.length - 1]);
		return items;
	}

	@Override
	public List<String> getEventIds() {
		if (eventids == null) {
			this.eventids = new ArrayList<>(eventnodes.length);
			for (int i = 0; i < eventnodes.length; i++) {
				EventNode node = this.eventnodes[i];
				final String eventId = node.getStringEventId();
				if (!eventids.contains(eventId)) {
					eventids.add(eventId);
				}
			}
		}
		return Collections.unmodifiableList(this.eventids);
	}

	/**
	 * Checks the pattern for consistency and validity.
	 * @return true iff the pattern is valid, false otherwise
	 */
	public boolean isValid() {
		if (this.isValid == null) {
			this.isValid = this.checkPattern(this.getPatternItemsInIntegerIdOrder());
		}
		return this.isValid;
	}

	private boolean checkPattern(List<HTPItem> items) {
		if(items.size() == 0) {
			return true;
		}

		Map<String, Integer> m = new HashMap<>();

		for(int i = 0; i < items.size(); i+=2) {
			if((i < items.size()-1 && !(items.get(i) instanceof EventNode) && !(items.get(i+1) instanceof OrderRelation))
					|| (i == items.size()-1 && !(items.get(i) instanceof PointEventNode) && !(items.get(i) instanceof IntervalEndEventNode))) {
				return false;
			}
			if(items.get(i) instanceof IntervalStartEventNode) {
				if(!m.containsKey(((EventNode) items.get(i)).getStringEventId())) {
					m.put(((EventNode) items.get(i)).getStringEventId(), 0);
				}
				m.put(((EventNode) items.get(i)).getStringEventId(), m.get(((EventNode) items.get(i)).getStringEventId()) + 1);
			}

			if(items.get(i) instanceof IntervalEndEventNode) {
				if(!m.containsKey(((EventNode) items.get(i)).getStringEventId())) {
					m.put(((EventNode) items.get(i)).getStringEventId(), 0);
				}
				m.put(((EventNode) items.get(i)).getStringEventId(), m.get(((EventNode) items.get(i)).getStringEventId()) - 1);
			}
		}

		for(String s : m.keySet()) {
			if(m.get(s) != 0) {
				return false;
			}
		}

		return true;
	}

	@Override
	public OrderRelation small(int from, int to) {
		for (int i = from; i < to; i++) {
			if (orderrelations[i] == OrderRelation.SMALLER) {
				return OrderRelation.SMALLER;
			}
		}
		return OrderRelation.EQUAL;
	}
}
