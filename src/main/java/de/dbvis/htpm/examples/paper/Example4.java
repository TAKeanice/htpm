package de.dbvis.htpm.examples.paper;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;

import static de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder.buildFromSequence;

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
		
		HybridTemporalPattern p2 = buildFromSequence(s2).getPattern();
		HybridTemporalPattern p3 = buildFromSequence(s3).getPattern();
		HybridTemporalPattern p4 = buildFromSequence(s4).getPattern();
		HybridTemporalPattern p5 = buildFromSequence(s5).getPattern();
		
		System.out.println(">>"+s1.occur(p2));
		System.out.println(">>"+s1.occur(p3));
		System.out.println(">>"+s1.occur(p4));
		System.out.println(">>"+s1.occur(p5));
	}
}
