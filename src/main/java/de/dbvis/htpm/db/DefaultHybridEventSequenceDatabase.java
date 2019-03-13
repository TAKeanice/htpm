package de.dbvis.htpm.db;

import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultHybridEventSequenceDatabase implements HybridEventSequenceDatabase {
	protected List<HybridEventSequence> sequences;

	protected Map<String, Integer> sequenceIDs;
	
	protected Map<HybridTemporalPattern, Double> support_cache;
	
	public DefaultHybridEventSequenceDatabase() {
		this.sequences = new ArrayList<>();
		this.support_cache = new ConcurrentHashMap<>();
	}
	
	public void add(HybridEventSequence seq) {
		this.sequences.add(seq);
	}
	
	public void remove(HybridEventSequence seq) {
		this.sequences.remove(seq);
	}

	@Override
	public int size() {
		return this.sequences.size();
	}

	public List<HybridEventSequence> getSequences() {
		return new ArrayList<>(this.sequences);
	}

	@Override
	public HybridEventSequence getSequence(final String id) {
		for(HybridEventSequence s : sequences) {
			if(s.getSequenceId().equals(id)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public double support(final HybridTemporalPattern p) {
		if(p == null) {
			return 0.d;
		}
		if(!this.support_cache.containsKey(p)) {
			this.support_cache.put(p, this.calcSupport(p));
		}
		return this.support_cache.get(p);
	}
	
	protected double calcSupport(HybridTemporalPattern p) {
		double i = 0.d;
		for(HybridEventSequence seq : this.sequences) {
			if(seq.supports(p)) {
				i++;
			}
		}
		return i / (double) sequences.size();
	}
	
	@Override
	public List<Occurrence> occurrences(HybridTemporalPattern p) {
		List<Occurrence> ors = new ArrayList<>();
		
		for(HybridEventSequence seq: this.sequences) {
			ors.addAll(seq.occur(p));
		}
		
		return ors;
	}
	
	public String toString() {
		String s = "";
		for(HybridEventSequence seq : this.sequences) {
			s += seq.toString()+"\n";
		}
		return s.substring(0, s.length()-1);
	}

}
