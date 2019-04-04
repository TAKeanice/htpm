package de.dbvis.htpm.htp.eventnodes;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This final class implements a PointEventNode.
 * @author Wolfgang Jentner
 *
 */
public final class PointEventNode extends EventNode {

	public PointEventNode(EventNode node) {
		super(node);
		if (!(node instanceof PointEventNode)) {
			throw new IllegalArgumentException("Point event node can only be created from other point event node");
		}
	}

	/**
	 * Creates a new PointEventNode based on an id and a time point.
	 * This PointEventNode will not be associated to an HybridEvent.
	 * @param id the id
	 */
	public PointEventNode(String id) {
		super(id);
	}
	
	/**
	 * Returns the String representation as is defined in the paper for HybridTemporalPatterns.
	 * A PointEventNode is simply represented by its id.
	 * @return A string representing this PointEventNode.
	 */
	@Override
	public String toString() {
		return this.getStringEventId();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(5, 17)
				.append(id)
				.toHashCode();
	}

	/**
	 * Checks for equality, true if the other object is also a PointEventNode and their ids are equal
	 * @param o the object (HTPItem) to check against with
	 * @return true if the conditions defined above are satisfied, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		return o instanceof PointEventNode
				&& ((PointEventNode) o).id == this.id;
	}
}
