package de.dbvis.htpm.util;

import java.util.EventObject;

/**
 * An HTPMEvent contains two basic information.
 * At first, it holds the current generation; second,
 * it holds the number of found and supported patterns in this
 * generation.
 * An exact time in milliseconds is also stored.
 * @author Wolfgang Jentner
 *
 */
public class HTPMEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3396562748039288595L;
	
	private long when;
	private int generation;
	private int number_of_patterns;

	/**
	 * Creates a new HTPMEvent.
	 * @param arg0 - the Object that fired the event.
	 * @param generation - the current generation.
	 * @param number_of_patterns - the number of supported patterns in that generation. 
	 * @param when - the timestamp in milliseconds
	 */
	public HTPMEvent(Object arg0, int generation, int number_of_patterns, long when) {
		super(arg0);
		this.generation = generation;
		this.number_of_patterns = number_of_patterns;
		this.when = when;
	}
	
	/**
	 * Creates a new HTPMEvent.
	 * @param arg0 - the Object that fired the event.
	 * @param generation - the current generation.
	 * @param number_of_patterns - the number of supported patterns in that generation. 
	 */
	public HTPMEvent(Object arg0, int generation, int number_of_patterns) {
		this(arg0, generation, number_of_patterns, System.currentTimeMillis());
	}
	
	/**
	 * Returns the current generation.
	 * @return the generation.
	 */
	public int getGeneration() {
		return this.generation;
	}
	
	/**
	 * Returns the number of supported patterns in the current generation.
	 * @return the number of supported patterns.
	 */
	public int getNumberOfPatterns() {
		return this.number_of_patterns;
	}
	
	/**
	 * Returns the timestamp of the event in milliseconds.
	 * @return the timestamp of the event.
	 */
	public long getWhen() {
		return this.when;
	}
}
