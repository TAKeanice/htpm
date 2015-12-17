package de.dbvis.htpm.htp.eventnodes;

/**
 * A very general interface, it is implemented by the EventNodes and OrderRelations
 * and represents all the items that may occur in a HybridTemporalPattern.
 * All HTPItems have to be immutable.
 * @author Wolfgang Jentner
 *
 */
public interface HTPItem {
	
	/**
	 * Checks if a HTPItem is equal to another HTPItem.
	 * Therfore, both items at least have to be of the same type.
	 * Equality may be defined individually.
	 * @param o the HTPItem to check against
	 * @return true if this item is equal to <code>o</code>, false otherwise
	 */
	boolean equals(Object o);
}
