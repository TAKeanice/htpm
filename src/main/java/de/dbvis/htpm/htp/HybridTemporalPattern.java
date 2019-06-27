package de.dbvis.htpm.htp;

import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.htp.eventnodes.HTPItem;
import de.dbvis.htpm.htp.eventnodes.OrderRelation;

import java.util.List;

/**
 * A HybridTemporalPattern represents basically the ordering of events.
 * According to the AllanRelations there are several possibilities of how events
 * can interfer with each other.
 * 
 * @author Wolfgang Jentner
 *
 */
public interface HybridTemporalPattern extends Comparable<HybridTemporalPattern> {
	
	/**
	 * Returns a String representation of the pattern similar to the definition in the paper.
	 * @return a String representation of the pattern (same as patternStr)
	 */
	public String toString();
	
	/**
	 * Returns the pattern string (with items in string id order).
	 * @return the pattern string
	 */
	public String patternStr();

	/**
	 * Pattern string for some nodes only, in string id order
	 * @param indices the numbers of the nodes
	 * @return pattern string which only includes nodes in indices
	 */
	public String partialPatternStr(int... indices);

	/**
	 * get number of event nodes
	 * @return same number as getEventNodes.size()
	 */
	public int size();

	/**
	 * @param i the number of the eventnode, 0<=i<getEventNodes().size()
	 * @return the event node at position i
	 */
	public EventNode getEventNode(int i);

	/**
	 * Returns a list of EventNodes which are in that pattern, in integer id order.
	 * @return a list of EventNodes
	 */
	public List<EventNode> getEventNodes();

	/**
	 * Returns a list of OrderRelations which are between EventNodes in the pattern.
	 * @return the OrderRelations of this pattern
	 */
	public List<OrderRelation> getOrderRelations();

	/**
	 * The length is defined by the number of events.
	 * Start and end node of the same event are counted as one.
	 * @return the number of events in the pattern
	 */
	public int length();
	
	/**
	 * The equality of a pattern is checked by the pattern itself.
	 * @param o - another object / pattern
	 * @return true if this and o describe the same pattern, false otherwise
	 */
	public boolean equals(Object o);
	
	/**
	 * A hash code based on the pattern.
	 * Necessary for the HTPM algorithm since multiple HashMaps are used there.
	 * Two equal patterns (where equals returns true) must also return the same hash
	 * @return the hash code of the pattern
	 */
	public int hashCode();
	
	/**
	 * Returns the complete ordered array of pattern items. These may be OrderRelations or any type of EventNodes.
	 * @return an array of ordered pattern items
	 */
	public List<HTPItem> getPatternItemsInIntegerIdOrder();

	/**
	 * Returns the complete ordered array of pattern items. These may be OrderRelations or any type of EventNodes.
	 * @return an array of ordered pattern items
	 */
	public List<HTPItem> getPatternItemsInStringIdOrder();
	
	/**
	 * Returns a list of all EventIds contained in the pattern.
	 * @return a list of all EventIds
	 */
	public List<String> getEventIds();

	/**
	 * Calculates the order relation between nodes
	 * @param from the index of the first node
	 * @param to the index of the second node
	 */
	public OrderRelation small(int from, int to);
}
