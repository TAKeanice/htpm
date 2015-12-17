package de.dbvis.htpm.examples.paper;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;

/**
 * This example shows how a hybrid temporal pattern looks like.
 * 
 * @author Wolfgang Jentner
 *
 */
public class Example3 {
	public static void main(String[] args) {
		HybridEventSequence s = new DefaultHybridEventSequence("1");
		s.add(new DefaultHybridEvent("a", 3,8));
		s.add(new DefaultHybridEvent("b", 3,5));
		s.add(new DefaultHybridEvent("b", 3));
		s.add(new DefaultHybridEvent("a", 3,9));
		s.add(new DefaultHybridEvent("a", 4,9));
		
		HybridTemporalPattern p = new DefaultHybridTemporalPattern("1", s);
		
		System.out.println(p.toString());
		System.out.println("Length: "+p.length());
	}
}
