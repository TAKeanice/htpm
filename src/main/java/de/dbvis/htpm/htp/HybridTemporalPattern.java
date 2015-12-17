package de.dbvis.htpm.htp;

import java.util.List;

import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.htp.eventnodes.HTPItem;

/**
 * A HybridTemporalPattern represents basically the ordering of events.
 * According to the AllanRelations there are several possibilities of how events
 * can interfer with each other.
 * 
 * @author Wolfgang Jentner
 *
 */
public interface HybridTemporalPattern {
	
	/**
	 * Returns the id of the pattern.
	 * @return the id of the pattern
	 */
	public String getPatternId();
	
	/**
	 * Returns a String representation of the pattern similar to the definition in the paper.
	 * @return a String representation of the pattern
	 */
	public String toString();
	
	/**
	 * Returns the pattern string without the prefix of the HybridTemporalPattern.
	 * @return the pattern string without prefix
	 */
	public String patternStr();
	
	/**
	 * Returns a list of EventNodes which are in that pattern.
	 * @return a list of EventNodes
	 */
	public List<EventNode> getEventNodes();

	/**
	 * The length defines the number of event occurences. 
	 * Not the number of event nodes.
	 * @return
	 */
	public int length();
	
	/**
	 * The equality of a pattern is checked by the pattern itself, not by its id.
	 * @param o - another object / pattern
	 * @return true if the two patterns are equal and contain the same pattern, false otherwise
	 */
	public boolean equals(Object o);
	
	/**
	 * A hash code based on the string representation of the pattern.
	 * Necessary for the HTPM algorithm since multiple HashMaps are used there.
	 * @return the hash code of the pattern
	 */
	public int hashCode();
	
	/**
	 * Returns the complete ordered array of pattern items. These may be OrderRelations or any type of EventNodes.
	 * @return an array of ordered pattern items
	 */
	public HTPItem[] getPatternItems();
	
	/**
	 * Returns a list of all EventIds.
	 * @return a list of all EventIds
	 */
	public List<String> getEventIds();
	
	/**
	 * Deletes the last event of the pattern according to the definition in the paper.
	 * Returns a new HybridTemporalPattern with the same id.
	 * @return a new HybridTemporalPattern with the same id where the last event is deleted
	 */
	public HybridTemporalPattern deleteLastEvent();
	
	/**
	 * Checks the pattern for consistency and validity.
	 * @return true iff the pattern is valid, false otherwise
	 */
	public boolean isValid();
}
