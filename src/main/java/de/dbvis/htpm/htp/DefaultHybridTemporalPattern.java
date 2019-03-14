package de.dbvis.htpm.htp;

import de.dbvis.htpm.htp.eventnodes.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

public class DefaultHybridTemporalPattern implements HybridTemporalPattern {

	private final EventNode[] eventnodes;
	private final OrderRelation[] orderrelations;

	private HybridTemporalPattern prefix;

	//stored for performance reasons
	private Integer length = null;
	private String patternstr = null;
	private Integer hashcode = null;
	private List<String> eventids = null;
	private Boolean isValid = null;
	private List<HTPItem> patternItems = null;

	public DefaultHybridTemporalPattern(List<EventNode> eventnodes, List<OrderRelation> orderrelations,
										HybridTemporalPattern prefix) {
		this.prefix = prefix;
		this.eventnodes = eventnodes.toArray(new EventNode[0]);
		this.orderrelations = orderrelations.toArray(new OrderRelation[0]);
	}

	public DefaultHybridTemporalPattern(String pattern) {
		List<EventNode> eventnodes = new ArrayList<>();
		List<OrderRelation> orderrelations = new ArrayList<>();

		//this is a hack, adding the = in the end of the pattern makes the pattern
		//iteself invalid, but the loop will continue once more and also adds
		//the last EventNode
		char[] p = (pattern + "=").toCharArray();

		StringBuilder id = new StringBuilder(100);
		StringBuilder oc = new StringBuilder(100);
		OrderRelation lastRel = null;
		boolean parsingOccurencemark = false;
		boolean isStartEvent = false;
		for(char c : p) {
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

		this.eventnodes = eventnodes.toArray(new EventNode[0]);
		this.orderrelations = orderrelations.toArray(new OrderRelation[0]);
	}

	public String toString() {
		return this.patternStr();
	}

	@Override
	public String patternStr() {
		if (patternstr == null) {
			StringBuilder str = new StringBuilder("(");
			for (int i = 0; i < orderrelations.length; i++) {
				str.append(eventnodes[i]);
				str.append(orderrelations[i]);
			}
			str.append(eventnodes[eventnodes.length - 1]);
			str.append(")");
			this.patternstr = str.toString();
		}
		return this.patternstr;
	}

	@Override
	public List<EventNode> getEventNodes() {
		return Arrays.asList(eventnodes);
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
		if (o instanceof DefaultHybridTemporalPattern) {
			DefaultHybridTemporalPattern other = (DefaultHybridTemporalPattern) o;
			return Arrays.equals(this.eventnodes, other.eventnodes)
					&& Arrays.equals(this.orderrelations, other.orderrelations);
		} else if(o instanceof HybridTemporalPattern) {
			HybridTemporalPattern other = (HybridTemporalPattern) o;
			return this.patternStr().equals(other.patternStr());
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (hashcode == null) {
			HashCodeBuilder hcb = new HashCodeBuilder(13, 31);
			hcb.append(eventnodes);
			this.hashcode = hcb.toHashCode();
			//String patternstr = this.patternStr();
			//this.hashcode = patternstr.hashCode();
		}
		return this.hashcode;
	}

	@Override
	public List<HTPItem> getPatternItems() {
		if (this.patternItems == null) {
			this.patternItems = new ArrayList<>(this.orderrelations.length * 2 + 1);
			for (int i = 0; i < orderrelations.length; i++) {
				this.patternItems.add(eventnodes[i]);
				this.patternItems.add(orderrelations[i]);
			}
			this.patternItems.add(eventnodes[eventnodes.length - 1]);
			this.patternItems = Collections.unmodifiableList(this.patternItems);
		}
		return patternItems;
	}

	@Override
	public List<String> getEventIds() {
		if (eventids == null) {
			this.eventids = new ArrayList<>(eventnodes.length);
			for (int i = 0; i < eventnodes.length; i++) {
				EventNode node = this.eventnodes[i];
				final String eventId = node.getStringEventNodeId();
				if (!eventids.contains(eventId)) {
					eventids.add(eventId);
				}
			}
		}
		return Collections.unmodifiableList(this.eventids);
	}

	private static OrderRelation small(List<OrderRelation> ors) {
		for(OrderRelation o : ors) {
			if(o != null && o.equals(OrderRelation.SMALLER)) {
				return OrderRelation.SMALLER;
			}
		}
		return OrderRelation.EQUAL;
	}

	@Override
	public boolean isValid() {
		if (this.isValid == null) {
			this.isValid = this.checkPattern(this.getPatternItems());
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
				if(!m.containsKey(((EventNode) items.get(i)).getStringEventNodeId())) {
					m.put(((EventNode) items.get(i)).getStringEventNodeId(), 0);
				}
				m.put(((EventNode) items.get(i)).getStringEventNodeId(), m.get(((EventNode) items.get(i)).getStringEventNodeId()) + 1);
			}

			if(items.get(i) instanceof IntervalEndEventNode) {
				if(!m.containsKey(((EventNode) items.get(i)).getStringEventNodeId())) {
					m.put(((EventNode) items.get(i)).getStringEventNodeId(), 0);
				}
				m.put(((EventNode) items.get(i)).getStringEventNodeId(), m.get(((EventNode) items.get(i)).getStringEventNodeId()) - 1);
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
	public HybridTemporalPattern getPrefix() {
		return prefix;
	}
}
