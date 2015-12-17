package de.dbvis.htpm.htp.eventnodes;

import de.dbvis.htpm.hes.events.HybridEvent;

/**
 * This abstract class implements a general IntervalEventNode and
 * extends the definition of an EventNode by an occurrence mark.
 * This occurrence mark is necessary in order to match start and end interval events
 * in a given sequence. The match is done by their id and the occurrence mark.
 * @author Wolfgang Jentner
 *
 */
public abstract class IntervalEventNode extends EventNode {
	/**
	 * The occurrence mark
	 */
	protected int occurrencemark;
	
	/**
	 * Creates a new IntervalEventNode based on a HybridEvent, a given occurrencemark,
	 * has to know if this constructor is called by an start or end interval node.
	 * This IntervalEventNode will be associated to the given HybridEvent.
	 * @param hybridevent the HybridEvent that provides the information
	 * @param occurrencemark the occurrence mark
	 * @param isendevent true if called by IntervalEndEventNode
	 */
	protected IntervalEventNode(HybridEvent hybridevent, int occurrencemark, boolean isendevent) {
		super(hybridevent, false, isendevent);
		this.occurrencemark = occurrencemark;
	}
	
	/**
	 * Creates a new IntervalEventNode by an explicit id, time point and occurrence mark.
	 * This IntervalEventNode will not be associated to an HybridEvent.
	 * @param id
	 * @param timepoint
	 * @param occurencemark
	 */
	protected IntervalEventNode(String id, double timepoint, int occurencemark, boolean endEvent) {
		super(id, timepoint, false, endEvent);
		this.occurrencemark = occurencemark;
	}
	
	/**
	 * Returns the occurrence mark of the IntervalEventNode.
	 * @return the occurrence mark
	 */
	public int getOccurrenceMark() {
		return this.occurrencemark;
	}
	
	public abstract String toString();
	
	public abstract boolean equals(Object o);
	
	public abstract boolean isCorrespondingOppositeNode(Object o);
}
