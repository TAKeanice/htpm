package de.dbvis.htpm.util;

/**
 * The HTPMListener can be added to get a basic
 * status update of the running HTPM algorithm.
 * The algorithm will fire an event for each pattern-generation.
 * 
 * @author Wolfgang Jentner
 *
 */
public interface HTPMListener {
	/**
	 * This event will be fired as soon as one generation
	 * is calculated.
	 * @param event - the HTPMEvent to be fired.
	 */
	public void generationCalculated(HTPMEvent event);
}
