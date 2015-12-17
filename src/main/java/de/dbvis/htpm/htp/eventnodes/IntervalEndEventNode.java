package de.dbvis.htpm.htp.eventnodes;

import de.dbvis.htpm.hes.events.HybridEvent;

public final class IntervalEndEventNode extends IntervalEventNode {
	
	/**
	 * Creates a new IntervalEndEventNode based on the information of the HybridEvent
	 * and the occurrence mark. This IntervalEndEventNode will be associated to the
	 * given HybridEvent.
	 * @param hybridevent the HybridEvent
	 * @param occurrencemark the occurrence mark
	 */
	public IntervalEndEventNode(HybridEvent hybridevent, int occurrencemark) {
		super(hybridevent, occurrencemark, true);
	}
	
	/**
	 * Creates a new IntervalEndEvent based on a given id, time point and occurrence mark.
	 * This IntervalEndEventNode will not be associated to an HybridEvent.
	 * @param id the id
	 * @param timepoint the time point
	 * @param occurencemark the occurrence mark
	 */
	public IntervalEndEventNode(String id, double timepoint, int occurencemark) {
		super(id, timepoint, occurencemark, true);
	}
	
	/**
	 * An IntervalEndEventNode is represented by its id, followed by '-' and its occurrence mark.
	 * As defined in the paper.
	 * @return A string representing the IntervalEndEventNode
	 */
	@Override
	public String toString() {
		return this.getEventNodeId()+"-"+this.getOccurrenceMark();
	}
	
	/**
	 * The equality is defined by the same instance, the id and the occurrence mark.
	 * @param o the HTPItem to check against
	 * @return true if the conditions stated above are satisfied, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof IntervalEndEventNode && ((IntervalEndEventNode) o).getEventNodeId().equals(this.getEventNodeId()) && ((IntervalEndEventNode) o).getOccurrenceMark() == this.getOccurrenceMark();
	}
	
	/**
	 * Checks if this node is the corresponding opposite node to an IntervalStartEventNode.
	 * This is based on the id and the occurrence mark.
	 * @param o the opposite node
	 * @return true if the conditions stated above are satisfied, false otherwise
	 */
	@Override
	public boolean isCorrespondingOppositeNode(Object o) {
		if(o instanceof IntervalStartEventNode) {
			IntervalEventNode e = (IntervalEventNode) o;
			if(this.getEventNodeId().equals(e.getEventNodeId()) 
				&& this.getOccurrenceMark() == e.getOccurrenceMark()) {
				return true;
			}
		}
		return false;
	}
}
