package de.dbvis.htpm.occurrence;

import de.dbvis.htpm.hes.HybridEventSequence;

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
	protected final HybridEventSequence seq;
	
	/**
	 * A list holding the OccurrencePoints
	 */
	protected final OccurrencePoint[] ops;

	/**
	 * Holds the child - canonical parent relation for this occurrence.
	 * The canonical parent of an occurrence must be from the same sequence,
	 * and have the same length-1 occurrences as temporal prefix
	 */
	protected Occurrence prefix;

	public DefaultOccurrence(HybridEventSequence seq, List<OccurrencePoint> ops) {
		this(seq, ops, null);
	}

	public DefaultOccurrence(HybridEventSequence seq, List<OccurrencePoint> ops, Occurrence prefix) {
		if(seq == null) {
			throw new NullPointerException("DefaultHybridEventSequence must not be null");
		}
		this.seq = seq;
		this.ops = ops.toArray(new OccurrencePoint[0]);
		this.prefix = prefix;
	}
	
	@Override
	public HybridEventSequence getHybridEventSequence() {
		return this.seq;
	}
	
	@Override
	public List<OccurrencePoint> ops() {
		return Arrays.asList(this.ops);
	}

	@Override
	public OccurrencePoint get(int i) {
		return this.ops[i];
	}

	@Override
	public int size() {
		return this.ops.length;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getHybridEventSequence().getSequenceId());
		sb.append("(");
		for(int i = 0; i < ops.length; i++) {
			sb.append(ops[i].getTimePoint());
			if (i < ops.length - 1) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	public boolean equals(Object o) {
		if(o instanceof Occurrence) {
			Occurrence o1 = (Occurrence) o;
			
			//check HES.ID
			if(!this.getHybridEventSequence().getSequenceId().equals(o1.getHybridEventSequence().getSequenceId())) {
				return false;
			}
			
			//check same size
			if(this.size() != o1.size()) {
				return false;
			}
			
			//check timepoints of OccurrencePoints in specific order
			for(int i = 0; i < o1.size(); i++) {
				if(this.get(i).getTimePoint() != o1.get(i).getTimePoint()) {
					return false;
				}
			}
			
			return true;
		}
		return false;
	}

	public Occurrence getPrefix() {
		return prefix;
	}
}
