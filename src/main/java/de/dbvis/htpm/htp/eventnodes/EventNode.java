package de.dbvis.htpm.htp.eventnodes;

import de.dbvis.htpm.util.UniqueIDConverter;

import java.util.Comparator;

/**
 * This abstracts class implements a HTPItem as well as an OccurrencePoint, it is also
 * comparable.
 * An EventNode is the basic, abstract object and must be defined further, either as
 * a PointEventNode or an IntervalEventNode.
 * @author Wolfgang Jentner
 *
 */
public abstract class EventNode implements HTPItem, Comparable<EventNode> {

	public final int id;

	protected EventNode(EventNode node) {
		this.id = node.id;
	}

	/**
	 * Creates a new EventNode without any association to a HybridEvent, for use in a pattern.
	 * @param id the id of the EventNode
	 */
	protected EventNode(String id) {
		if (id == null) {
			throw new NullPointerException("id of pattern must not be null");
		}
		this.id = UniqueIDConverter.getIntegerId(id);
	}
	
	/**
	 * Returns the id of the EventNode
	 * @return the id of the EventNode
	 */
	public String getStringEventId() {
		return UniqueIDConverter.getStringId(this.id);
	}

	public int getIntegerEventID() {
		return id;
	}

	/**
	 * This abstract method must be implemented by the extending classes,
	 * the representation varies by the type of EventNode.
	 * @return A representation of the EventNode
	 */
	public abstract String toString();

	/*-------------------------------------------------+
	| Comparing event nodes                            |
	+-------------------------------------------------*/

	/**
	 * EventNodes may be stored in datastructures that use hashing to compare objects
	 * The hash function of an eventnode must yield equal hashes for eventnodes that equal,
	 * not only when they are identical.
	 */
	@Override
	public abstract int hashCode();

	/**
	 * This abstract method must be implemented by the extending classes,
	 * the equality is defined by each individual type.
	 * @param o the EventNode to check against
	 * @return true if the EventNodes are equal, false otherwise
	 */
	@Override
	public abstract boolean equals(Object o);

	private static Comparator<EventNode> integerIdComparator = Comparator.comparingInt(EventNode::getIntegerEventID);

	private static Comparator<EventNode> stringIdComparator = Comparator.comparing(EventNode::getStringEventId);

	private static Comparator<EventNode> typeComparator = (a, b) -> {
		int typeComparison = 0;
		if (a instanceof IntervalEndEventNode) {
			if (b instanceof IntervalStartEventNode || b instanceof PointEventNode) {
				typeComparison = 1;
			}
		} else if (a instanceof PointEventNode) {
			if (b instanceof IntervalStartEventNode) {
				typeComparison = 1;
			} else if (b instanceof IntervalEndEventNode) {
				typeComparison = -1;
			}
		} else if (a instanceof IntervalStartEventNode) {
			if (b instanceof PointEventNode || b instanceof IntervalEndEventNode) {
				typeComparison = -1;
			}
		}
		return typeComparison;
	};

	private static Comparator<EventNode> occurrenceMarkComparator = (a, b) -> {
		int occurrenceMarkComparison = 0;
		if (a instanceof IntervalEventNode && b instanceof IntervalEventNode) {
			occurrenceMarkComparison = Integer.compare(((IntervalEventNode)a).occurrencemark, ((IntervalEventNode)b).occurrencemark);
		}
		return occurrenceMarkComparison;
	};

	private static Comparator<EventNode> eventNodeWithIntIdComparator =
			integerIdComparator.thenComparing(typeComparator).thenComparing(occurrenceMarkComparator);

	private static Comparator<EventNode> getEventNodeWithStringIdComparator =
			stringIdComparator.thenComparing(typeComparator).thenComparing(occurrenceMarkComparator);

	@Override
	public int compareTo(EventNode o) {
		return compareByIntId(this, o);
	}

	public static int compareByIntId(EventNode a, EventNode b) {
		return eventNodeWithIntIdComparator.compare(a, b);
	}

	public static int compareByStringId(EventNode a, EventNode b) {
		return getEventNodeWithStringIdComparator.compare(a, b);
	}
}
