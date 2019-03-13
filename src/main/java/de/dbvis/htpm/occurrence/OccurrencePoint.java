package de.dbvis.htpm.occurrence;

import de.dbvis.htpm.hes.events.HybridEvent;

/**
 * The OccurrencePoint is a time point containing an HybridEvent.
 * 
 * @author Wolfgang Jentner
 */
public interface OccurrencePoint extends Comparable<OccurrencePoint> {
	
	/**
	 * Returns the time point of the occurrence.
	 * @return the time point of the occurrence.
	 */
	double getTimePoint();
	
	/**
	 * Returns the HybridEvent that is associated with the occurrence.
	 * This method may also return null if there is no HybridEvent associated.
	 * @return the HybridEvent, may be null.
	 */
	HybridEvent getHybridEvent();
}
