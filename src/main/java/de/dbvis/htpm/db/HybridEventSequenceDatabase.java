package de.dbvis.htpm.db;

import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public interface HybridEventSequenceDatabase {
	/**
	 * Adds an HybridEventSequence to the database
	 * @param seq the HybridEventSequence to add, must not be null
	 */
	void add(final HybridEventSequence seq);

	/**
	 * Removes a HybridEventSequence from the database
	 * @param seq the HybridEventSequence to remove
	 */
	void remove(final HybridEventSequence seq);

	/**
	 * Returns the number of HybridEventSequences stored in the database
	 * @return the number of HybridEventSequences
	 */
	int size();

	/**
	 * Returns a List of all the HybridEventSequences contained in the database
	 * @return a List of all HybridEventSequences
	 */
	List<HybridEventSequence> getSequences();

	/**
	 * Searches for a HybridEventSequence by a given id
	 * @param id the id of the HybridEventSequence
	 * @return the HybridEventSequence or null if not found
	 */
	HybridEventSequence getSequence(final String id);

	/**
	 * Calculates the support of a given HybridTemproalPattern
	 * If p is null, the support will be 0.
	 * @param p the HybridTemporalPattern
	 * @return the support (0-1) of the HybridTemporalPattern
	 */
	double support(final HybridTemporalPattern p);

	/**
	 * Returns all the Occurrences that can be found in all HybridEventSequences that are stored in the database.
	 * @param p the HybridTemporalPattern
	 * @return a List of Occurrences that can be found with the given HybridTemporalPattern
	 */
	List<Occurrence> occurrences(final HybridTemporalPattern p);
}
