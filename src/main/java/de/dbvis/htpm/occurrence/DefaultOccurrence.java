package de.dbvis.htpm.occurrence;

import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.HybridEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * This class implements an Occurrence. The class is mainly intended for 
 * internal usage. It also provides two methods to guarantee the mutability
 * of this object.
 * 
 * @author Wolfgang Jentner
 *
 */
public class DefaultOccurrence implements Occurrence {
	/**
	 * The associated HybridEventSequence
	 */
	private final HybridEventSequence seq;
	
	/**
	 * A list holding the OccurrencePoints
	 */
	private final HybridEvent[] ops;

	public DefaultOccurrence(HybridEventSequence seq, List<HybridEvent> ops) {
		if(seq == null) {
			throw new NullPointerException("HybridEventSequence must not be null");
		}
		this.seq = seq;
		this.ops = ops.toArray(new HybridEvent[0]);
	}
	
	@Override
	public HybridEventSequence getHybridEventSequence() {
		return this.seq;
	}
	
	@Override
	public List<HybridEvent> ops() {
		return Arrays.asList(this.ops);
	}

	@Override
	public HybridEvent get(int i) {
		return this.ops[i];
	}

	@Override
	public int size() {
		return this.ops.length;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getHybridEventSequence().getSequenceId());
		sb.append("(");
		for(int i = 0; i < ops.length; i++) {
			final HybridEvent op = ops[i];
			int firstIndex = ArrayUtils.indexOf(ops, op);
			int lastIndex = ArrayUtils.lastIndexOf(ops, op);
			sb.append(firstIndex == lastIndex ? op.getTimePoint()
					: (firstIndex == i ? op.getStartPoint() : op.getEndPoint()));
			if (i < ops.length - 1) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getHybridEventSequence()).append(ops()).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Occurrence) {
			final Occurrence other = (Occurrence) o;
			return new EqualsBuilder()
					.append(getHybridEventSequence(), other.getHybridEventSequence())
					.append(ops(), other.ops()).isEquals();
		}
		return false;
	}
}
