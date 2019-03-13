package de.dbvis.htpm.htp.eventnodes;

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
	public final int occurrencemark;

	protected IntervalEventNode(EventNode node, int occurrencemark) {
		super(node);
		this.occurrencemark = occurrencemark;
	}

	/**
	 * Creates a new IntervalEventNode by an explicit id, time point and occurrence mark.
	 * This IntervalEventNode will not be associated to an HybridEvent.
	 * @param id
	 * @param occurencemark
	 */
	protected IntervalEventNode(String id, int occurencemark) {
		super(id);
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
}
