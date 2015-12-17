package de.dbvis.htpm.hes;

import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

/**
 * A HybridEventSequence stores HybridEvents and provides some functions
 * to test wether a specific id, HybridEvent or HybridTemporalPattern occurrs
 * in the HybridEventSequence.
 * 
 * @author Wolfgang Jentner
 *
 */
public interface HybridEventSequence {
	
	/**
	 * Returns the HybridEventSequence id.
	 * @return The HybridEventSequence id
	 */
	String getSequenceId();
	
	/**
	 * Adds a new HybridEvent to the HybridEventSequence.
	 * If null no exception is thrown but nothing will be added.
	 * @param e The HybridEvent to add
	 */
	void add(HybridEvent e);
	
	/**
	 * Removes a HybridEvent from the sequence if it is in the HybridEventSequence.
	 * @param e The HybridEvent to remove
	 */
	void remove(HybridEvent e);
	
	/**
	 * Returns a modifiable but not mutable list of HybridEvents that are
	 * currently stored in the HybridEventSequence
	 * @return A list of HybridEvents that are currently stored in the HybridEventSequence
	 */
	List<HybridEvent> getEvents();
	
	/**
	 * Returns a list of Occurrences of HybridEvents that have the given
	 * id. If there is no such HybridEvent the list will be empty.
	 * @param hybridevent_id The id of a HybridEvent
	 * @return A list of Occurrences of the HybridEvent. May be empty but never null.
	 */
	List<Occurrence> occur(String hybridevent_id);
	
	/**
	 * Returns a list of Occurrences of HybridEvents whose id is equal to the given HybridEvent.
	 * This method is a wrapper method of <code>HybridEventSequence.occur(String id)</code>.
	 * It is not based on the equality of HybridEvents.
	 * If there is no such HybridEvent the list will be empty.
	 * @param hybridevent The HybridEvent (id) to search for.
	 * @return A list of Occurrences of the HybridEvent. May be empty but never null.
	 */
	List<Occurrence> occur(HybridEvent hybridevent);
	
	/**
	 * Returns a list of Occurrences of HybridEvents that match the given HybridTemporalPattern.
	 * If there are no such Occurrences the list will be empty.
	 * @param p The HybridTemporalPattern to search for.
	 * @return A list of Occurrences of the HybridTemproalPattern. May be empty but never null.
	 */
	List<Occurrence> occur(HybridTemporalPattern p);
	
	/**
	 * Returns true if the HybridEventSequence supports that pattern meaning that
	 * there is at least one Occurrence of the HybridTemporalPattern in the HybridEventSequence.
	 * @param p the HybridTemporalPattern to test with
	 * @return true if the HybridEventSequence supports the HybridTemporalPattern, false otherwise
	 */
	boolean supports(HybridTemporalPattern p);

	/**
	 * Checks if this occurrence really occurs in the sequence
	 * @param o the Occurrence to check
	 * @return true if o really occurs in the sequence, false otherwise
	 */
	boolean isValid(final Occurrence o);
}