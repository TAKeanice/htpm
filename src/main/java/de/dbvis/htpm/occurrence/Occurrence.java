package de.dbvis.htpm.occurrence;

import de.dbvis.htpm.hes.HybridEventSequence;

import java.util.List;

/**
 * An Occurrence is basically a sequence containing OccurrencePoints.
 * There is no guarantee by this interface that the OccurrencePoints are sorted.
 * The order is defined by the HybridEventSequence.order(...) methods as well as 
 * by the HTPM-Algorithm.
 * An Occurrence is immutable.
 * Each Occurrence is also always associated to a HybridEventSequence,
 * this connection states where the Occurrence comes from.
 * 
 * @author Wolfgang Jentner
 *
 */
public interface Occurrence {
	
	/**
	 * Returns the HybridEventSequence where the Occurrence comes from.
	 * @return A HybridEventSequence, never null
	 */
	public HybridEventSequence getHybridEventSequence();
	
	/**
	 * Returns an OccurrencePoint at a specific position.
	 * @param i the position in the list (0 <= i < Occurrence.size())
	 * @return
	 */
	public OccurrencePoint get(int i);
	
	/**
	 * Returns the size of the Occurrence which is equal to the number 
	 * of OccurrencePoints the Occurrence holds.
	 * @return the size of the Occurrence / number of OccurrencePoints
	 */
	public int size();

	/**
	 * Returns the list of occurrence points in order of occurrence
	 */
	public List<OccurrencePoint> ops();

	/**
	 * @return the canonical parent of this occurrence
	 */
	public Occurrence getPrefix();
	
	/**
	 * Returns a String representation of the Occurrence, the representation
	 * is defined in the paper. The representation starts with the id of the
	 * HybridEventSequence followed by the time points of the OccurrencePoints.
	 * <br/>Example: S1(1.0,2.0,3.0,4.0) where S1 is the id of the HybridEventSequence 
	 * @return a string representing the Occurrence
	 */
	public String toString();
	
	/**
	 * Returns true if two Occurrences are equal. The equality is defined by the same
	 * HybridEventSequenceId as well as the time points of the OccurrencePoints, they also
	 * must occur in the same order.
	 * @param o any object to test for equality
	 * @return true if it satisfies the conditions stated above, false otherwise
	 */
	public boolean equals(Object o);
}
