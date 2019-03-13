package de.dbvis.htpm.htp.eventnodes;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class IntervalEndEventNode extends IntervalEventNode {

	public IntervalEndEventNode(EventNode node, int occurrencemark) {
		super(node, occurrencemark);
		if (!(node instanceof IntervalEndEventNode)) {
			throw new IllegalArgumentException("Interval event node can only be created from other interval event node");
		}
	}

	/**
	 * Creates a new IntervalEndEvent based on a given id, time point and occurrence mark.
	 * This IntervalEndEventNode will not be associated to an HybridEvent.
	 * @param id the id
	 * @param occurencemark the occurrence mark
	 */
	public IntervalEndEventNode(String id, int occurencemark) {
		super(id, occurencemark);
	}
	
	/**
	 * An IntervalEndEventNode is represented by its id, followed by '-' and its occurrence mark.
	 * As defined in the paper.
	 * @return A string representing the IntervalEndEventNode
	 */
	@Override
	public String toString() {
		return this.getStringEventNodeId()+"-"+this.getOccurrenceMark();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 17)
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
		return o instanceof IntervalEndEventNode
				&& ((IntervalEndEventNode) o).id == this.id
				&& ((IntervalEndEventNode) o).occurrencemark == this.occurrencemark;
	}
}
