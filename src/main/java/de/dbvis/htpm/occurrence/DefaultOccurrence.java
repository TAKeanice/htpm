package de.dbvis.htpm.occurrence;

import de.dbvis.htpm.hes.HybridEventSequence;

import java.util.ArrayList;
import java.util.Iterator;
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
	protected HybridEventSequence seq;
	
	/**
	 * A list holding the OccurrencePoints
	 */
	protected List<OccurrencePoint> ops;
	
	/**
	 * Creates a new, empty DefaultOccurrence with a associated HybridEventSequence.
	 * @param seq the HybridEventSequence, may not be null
	 */
	public DefaultOccurrence(HybridEventSequence seq) {
		if(seq == null) {
			throw new NullPointerException("DefaultHybridEventSequence must not be null");
		}
		this.seq = seq;
		this.ops = new ArrayList<>();
	}
	
	@Override
	public HybridEventSequence getHybridEventSequence() {
		return this.seq;
	}
	
	@Override
	public Iterator<OccurrencePoint> iterator() {
		return this.ops.iterator();
	}

	@Override
	public OccurrencePoint get(int i) {
		return this.ops.get(i);
	}

	@Override
	public int size() {
		return this.ops.size();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(this.getHybridEventSequence().getSequenceId()+"(");
		for(OccurrencePoint op : this) {
			sb.append(op.getTimePoint());
			sb.append(",");
		}
		sb.setCharAt(sb.length()-1, ')');
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
	
	/**
	 * Adds an OccurrencePoint to the Occurrence.
	 * @param op the OccurrencePoint to add
	 */
	public void add(OccurrencePoint op) {
		if(op != null) {
			this.ops.add(op);
		}
	}
	
	/**
	 * Removes an OccurrencePoint from the Occurrence.
	 * @param op the OccurrencePoint to remove
	 */
	public void remove(OccurrencePoint op) {
		this.ops.remove(op);
	}
}
