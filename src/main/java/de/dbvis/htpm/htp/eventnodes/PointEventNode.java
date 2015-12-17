package de.dbvis.htpm.htp.eventnodes;

import de.dbvis.htpm.hes.events.HybridEvent;
/**
 * This final class implements a PointEventNode.
 * @author Wolfgang Jentner
 *
 */
public final class PointEventNode extends EventNode {

	/**
	 * Creates a new PointEventNode based on the information of the HybridEvent (id & time point).
	 * This PointEventNode will be associated to the HybridEvent
	 * @param hybridevent the HybridEvent
	 */
	public PointEventNode(HybridEvent hybridevent) {
		super(hybridevent, true, false);
	}
	
	/**
	 * Creates a new PointEventNode based on an id and a time point.
	 * This PointEventNode will not be associated to an HybridEvent.
	 * @param id the id
	 * @param timepoint the time point
	 */
	public PointEventNode(String id, double timepoint) {
		super(id, timepoint, true, false);
	}
	
	/**
	 * Returns the String representation as is defined in the paper for HybridTemporalPatterns.
	 * A PointEventNode is simply represented by its id.
	 * @return A string representing this PointEventNode.
	 */
	@Override
	public String toString() {
		return this.getEventNodeId();
	}
	
	/**
	 * Checks for equality, true if the other object is also a PointEventNode and their ids are equal
	 * @param o the object (HTPItem) to check against with
	 * @return true if the conditions defined above are satisfied, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if(o != null && o instanceof PointEventNode) {
			return ((PointEventNode) o).getEventNodeId().equals(this.getEventNodeId());
		}
		return false;
	}
}
