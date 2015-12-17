package de.dbvis.htpm.htp.eventnodes;

import de.dbvis.htpm.hes.events.HybridEvent;

/**
 * This final class implements an IntervalStartEventNode.
 * 
 * @author Wolfgang Jentner
 *
 */
public final class IntervalStartEventNode extends IntervalEventNode {
	
	/**
	 * Creates a new IntervalStartEventNode based on the information of the HybridEvent
	 * and the occurrence mark. This IntervalStartEventNode will be associated to the
	 * given HybridEvent.
	 * @param hybridevent the HybridEvent
	 * @param occurrencemark the occurrence mark
	 */
	public IntervalStartEventNode(HybridEvent hybridevent, int occurrencemark) {
		super(hybridevent, occurrencemark, false);
	}
	
	/**
	 * Creates a new IntervalStartEvent based on a given id, time point and occurrence mark.
	 * This IntervalStartEventNode will not be associated to an HybridEvent.
	 * @param id the id
	 * @param timepoint the time point
	 * @param occurrencemark the occurrence mark
	 */
	public IntervalStartEventNode(String id, double timepoint, int occurrencemark) {
		super(id, timepoint, occurrencemark, false);
	}
	
	/**
	 * An IntervalStartEventNode is represented by its id, followed by '+' and its occurrence mark.
	 * As defined in the paper.
	 * @returns A string representing the IntervalStartEventNode
	 */
	@Override
	public String toString() {
		return this.getEventNodeId()+"+"+this.getOccurrenceMark();
	}

	/**
	 * The equality is defined by the same instance, the id and the occurrence mark.
	 * @param o the HTPItem to check against
	 * @return true if the conditions stated above are satisfied, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if(o != null && o instanceof IntervalStartEventNode) {
			return ((IntervalStartEventNode) o).getEventNodeId().equals(this.getEventNodeId()) && ((IntervalStartEventNode) o).getOccurrenceMark() == this.getOccurrenceMark();
		}
		return false;
	}

	/**
	 * Checks if this node is the corresponding opposite node to an IntervalEndEventNode.
	 * This is based on the id and the occurrence mark.
	 * @param o the opposite node
	 * @return true if the conditions stated above are satisfied, false otherwise
	 */
	@Override
	public boolean isCorrespondingOppositeNode(Object o) {
		if(o instanceof IntervalEndEventNode) {
			IntervalEventNode e = (IntervalEventNode) o;
			if(this.getEventNodeId().equals(e.getEventNodeId()) 
				&& this.getOccurrenceMark() == e.getOccurrenceMark()) {
				return true;
			}
		}
		return false;
	}
}
