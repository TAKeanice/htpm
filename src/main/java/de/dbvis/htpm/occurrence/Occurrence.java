package de.dbvis.htpm.occurrence;

import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.EventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalStartEventNode;
import de.dbvis.htpm.htp.eventnodes.PointEventNode;

import java.util.List;

/**
 * An Occurrence is a sequence containing OccurrencePoints.
 * They are ordered by their time.
 * An Occurrence is immutable.
 * Each Occurrence is always associated to
 * a HybridEventSequence and a HybridTemporalPattern
 * thus the event types associated to the OccurrencePoints can be identified.
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
	 * @return the OccurrencePoint at a position i.
	 */
	public HybridEvent get(int i);

	/**
	 * Returns the size of the Occurrence which is equal to the number
	 * of OccurrencePoints the Occurrence holds.
	 * @return the size of the Occurrence / number of OccurrencePoints
	 */
	public int size();


	/**
	 * Returns the list of occurrence points in order of occurrence
	 */
	public List<HybridEvent> ops();
	
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

	static double getTimepoint(HybridTemporalPattern pattern, Occurrence occ, int i) {
		EventNode node = pattern.getEventNode(i);
		HybridEvent event = occ.get(i);
		return node instanceof PointEventNode ? event.getTimePoint()
				: (node instanceof IntervalStartEventNode ? event.getStartPoint() : event.getEndPoint());
	}
}
