package de.dbvis.htpm.htp.eventnodes;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This final class implements an IntervalStartEventNode.
 * 
 * @author Wolfgang Jentner
 *
 */
public final class IntervalStartEventNode extends IntervalEventNode {

	public IntervalStartEventNode(EventNode node, int occurrencemark) {
		super(node, occurrencemark);
		if (!(node instanceof IntervalStartEventNode)) {
			throw new IllegalArgumentException("Interval event node can only be created from other interval event node");
		}
	}

	/**
	 * Creates a new IntervalStartEvent based on a given id, time point and occurrence mark.
	 * This IntervalStartEventNode will not be associated to an HybridEvent.
	 * @param id the id
	 * @param occurrencemark the occurrence mark
	 */
	public IntervalStartEventNode(String id, int occurrencemark) {
		super(id, occurrencemark);
	}
	
	/**
	 * An IntervalStartEventNode is represented by its id, followed by '+' and its occurrence mark.
	 * As defined in the paper.
	 * @returns A string representing the IntervalStartEventNode
	 */
	@Override
	public String toString() {
		return this.getStringEventId()+"+"+this.getOccurrenceMark();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(7, 17)
				.append(id)
				.append(occurrencemark)
				.toHashCode();
	}

	/**
	 * The equality is defined by the same instance, the id and the occurrence mark.
	 * @param o the HTPItem to check against
	 * @return true if the conditions stated above are satisfied, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		return o instanceof IntervalStartEventNode
				&& ((IntervalStartEventNode) o).id == this.id
				&& ((IntervalStartEventNode) o).occurrencemark == this.occurrencemark;
	}
}
