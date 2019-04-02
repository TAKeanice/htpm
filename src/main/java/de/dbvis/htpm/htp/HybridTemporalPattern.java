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
	 * @return a String representation of the pattern
	 */
	public String toString();
	
	/**
	 * Returns the pattern string.
	 * @return the pattern string
	 */
	public String patternStr();
	
	/**
	 * Returns a list of EventNodes which are in that pattern.
	 * @return a list of EventNodes
	 */
	public List<EventNode> getEventNodes();

	/**
	 * Returns a list of OrderRelations which are between EventNodes in the pattern.
	 * @return the OrderRelations of this pattern
	 */
	public List<OrderRelation> getOrderRelations();

	/**
	 * The length defines the number of events.
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
	public List<HTPItem> getPatternItems();
	
	/**
	 * Returns a list of all EventIds contained in the pattern.
	 * @return a list of all EventIds
	 */
	public List<String> getEventIds();

	/**
	 * Checks the pattern for consistency and validity.
	 * @return true iff the pattern is valid, false otherwise
	 */
	public boolean isValid();

	public static int compare(HybridTemporalPattern first, HybridTemporalPattern second) {
		List<HTPItem> firstItems = first.getPatternItems();
		List<HTPItem> secondItems = second.getPatternItems();
		int result = Integer.compare(firstItems.size(), secondItems.size());
		for (int i = 0; i < firstItems.size() && result == 0; i++) {
			if (firstItems.get(i) instanceof EventNode) {
				result = ((EventNode) firstItems.get(i)).compareTo((EventNode) secondItems.get(i));
			} else {
				result = ((OrderRelation) firstItems.get(i)).compareTo((OrderRelation) secondItems.get(i));
			}
		}
		return result;
	}

	public static OrderRelation small(List<OrderRelation> ors) {
		for(OrderRelation o : ors) {
			if(o == OrderRelation.SMALLER) {
				return OrderRelation.SMALLER;
			}
		}
		return OrderRelation.EQUAL;
	}
}
