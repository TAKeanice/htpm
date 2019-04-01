package de.dbvis.htpm.examples.paper;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.htp.HybridTemporalPattern;

import static de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder.buildFromSequence;

/**
 * This example shows patterns and how the prefix patterns look like.
 * 
 * @author Wolfgang Jentner
 *
 */
public class Example6 {
	public static void main(String[] args) {
		DefaultHybridEventSequence s = new DefaultHybridEventSequence("S1");
		s.add(new DefaultHybridEvent("b", 6, 12));
		s.add(new DefaultHybridEvent("c", 6));
		s.add(new DefaultHybridEvent("a", 8, 12));
		
		HybridTemporalPattern p1 = buildFromSequence(s).getPattern();
		
		s = new DefaultHybridEventSequence("S1");
		s.add(new DefaultHybridEvent("b", 6, 12));
		s.add(new DefaultHybridEvent("c", 6));
		s.add(new DefaultHybridEvent("c", 8));
		
		HybridTemporalPattern p2 = buildFromSequence(s).getPattern();
		
		System.out.println(p1);
		System.out.println(p2);

		/* TODO: this test is now invalid, because we removed modifying method
		System.out.println("k-1");
		System.out.println(p1.deleteLastEvent());
		System.out.println(p2.deleteLastEvent());
		
		System.out.println(p1.deleteLastEvent().equals(p2.deleteLastEvent()));
		*/
		
		System.out.println(buildFromSequence(ExampleDatabase.getExample().getSequence("1")));
		//System.out.println(new HybridTemporalPattern("P1" ,ExampleDatabase.getExample().getSequence("S1")).getOccurences());
		
		System.out.println(ExampleDatabase.getExample().getSequence("1").occur(p1));
		System.out.println(ExampleDatabase.getExample().getSequence("2").occur(p1));
		System.out.println(ExampleDatabase.getExample().getSequence("3").occur(p1));
	}
}
