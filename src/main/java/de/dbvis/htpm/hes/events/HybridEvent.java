package de.dbvis.htpm.hes.events;


/**
 * A HybridEvent consists of an ID and a occurrence.
 * The event can be either of type point event or interval event.
 * A HybridEvent should be immutable.
 * 
 * 
 * @author Wolfgang Jentner
 */
public interface HybridEvent extends Comparable<HybridEvent> {
	/**
	 * Returns the event id. The String may not contain +,-,<,=
	 * @return A string containing the EventId.
	 */
	String getEventId();
	
	/**
	 * The start point of the event.
	 * @return the start point
	 */
	double getStartPoint();
	
	/**
	 * The end point of the event.
	 * Null if HybridEvent is not of type interval event.
	 * @return the end point or null if HybridEvent is not of type interval event
	 */
	Double getEndPoint();
	
	/**
	 * An alias method to the getStartPoint() method.
	 * @return the time point or start point
	 */
	double getTimePoint();
	
	/**
	 * Checks wether the HybridEvent is of type point event
	 * @return true if point event, false otherwise
	 */
	boolean isPointEvent();
	
	/**
	 * Compares an HybridEvent to another, true if the other object
	 * is of the same type, the event id is the same as well as the occurrence.
	 * @param o - The object to compare to.
	 * @return true if both HybridEvents, event id & occurrence is the same, false otherwise.
	 */
	boolean equals(Object o);

	int hashCode();
	
	/**
	 * Returns a String, follows the definitions used in the paper.
	 * <br/>Examples:
	 * <br/> - (a,6.0) a point based HybridEvent with the id "a" occurring at time point 6.
	 * <br/> - (b,6.0,12.0) an interval based HybridEvent with the id "b" occurring from 6 to 12.
	 * @return a String defined 
	 */
	String toString();
	
	/**
	 * Compares this HybridEvent with another HybridEvent by the start 
	 * points or time points respectively. If those are equal then the
	 * HybridEvents are compared by their type, a point based event occurs before an interval based event. 
	 * If this is also equal they are compared by their id in a lexicographic order.
	 * @param o the HybridEvent to compare to
	 * @return < 0 if this HybridEvent occurs before HybridEvent <code>o</code> (by the constraints defined above)
	 * <br/>= 0 if the HybridEvents are "equal", note that the end point may differ (by the constraints defined above)
	 * <br/>> 0 if this HybridEvent occurs after <code>o</code> (by the constraints defined above)
	 */
	int compareTo(HybridEvent o);
}
