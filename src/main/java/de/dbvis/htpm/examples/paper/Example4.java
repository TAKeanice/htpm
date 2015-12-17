package de.dbvis.htpm.examples.paper;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;

/**
 * This example shows how a hybrid temporal pattern is built and
 * how the occurrences within a sequence are determined.
 * 
 * @author Wolfgang Jentner
 *
 */
public class Example4 {
	public static void main(String[] args) {
		HybridEventSequence s1 = ExampleDatabase.getExample().getSequence("1");
		
		HybridEventSequence s2 = new DefaultHybridEventSequence("2");
		s2.add(new DefaultHybridEvent("c", 1));
		
		HybridEventSequence s3 = new DefaultHybridEventSequence("3");
		s3.add(new DefaultHybridEvent("a", 0, 1));
		
		HybridEventSequence s4 = new DefaultHybridEventSequence("4");
		s4.add(new DefaultHybridEvent("a", 0, 2));
		s4.add(new DefaultHybridEvent("b", 1, 3));
		
		HybridEventSequence s5 = new DefaultHybridEventSequence("5");
		s5.add(new DefaultHybridEvent("a", 0,2));
		s5.add(new DefaultHybridEvent("b", 0,3));
		
		HybridTemporalPattern p2 = new DefaultHybridTemporalPattern("2", s2);
		HybridTemporalPattern p3 = new DefaultHybridTemporalPattern("3", s3);
		HybridTemporalPattern p4 = new DefaultHybridTemporalPattern("4", s4);
		HybridTemporalPattern p5 = new DefaultHybridTemporalPattern("5", s5);
		
		System.out.println(">>"+s1.occur(p2));
		System.out.println(">>"+s1.occur(p3));
		System.out.println(">>"+s1.occur(p4));
		System.out.println(">>"+s1.occur(p5));
	}
}