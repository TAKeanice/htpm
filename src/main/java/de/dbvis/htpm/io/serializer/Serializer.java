package de.dbvis.htpm.io.serializer;

import java.util.List;

/**
 * This interface describe a very generic serializer that transform objects of
 * any type into an external representation.
 * 
 * @param <T>
 *            any class
 * @author Thorsten Meinl, University of Konstanz
 */
public interface Serializer<T> {
	/**
	 * Serializes a single object.
	 * 
	 * @param object
	 *            any object
	 */
	public void serialize(T object);

	/**
	 * Serializes a list of objects.
	 * 
	 * @param objects
	 *            a list of objects
	 */
	public void serialize(List<T> objects);

	/**
	 * Closes the serializer and ensures that all data is really serialized,
	 * buffers are flushed, etc.
	 */
	public void close();
}
