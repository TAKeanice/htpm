package de.dbvis.htpm.examples.paper;

import de.dbvis.htpm.db.DefaultHybridEventSequenceDatabase;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;

/**
 * This is the example database used in the paper.
 * 
 * @author Wolfgang Jentner
 *
 */
public class ExampleDatabase {
	public static HybridEventSequenceDatabase getExample() {
		HybridEventSequenceDatabase d = new DefaultHybridEventSequenceDatabase();
		
		HybridEventSequence s = new DefaultHybridEventSequence("1");
		
		s.add(new DefaultHybridEvent("c", 6));
		
		s.add(new DefaultHybridEvent("c", 8));
		s.add(new DefaultHybridEvent("a", 5, 10));
		s.add(new DefaultHybridEvent("b", 6, 12));
		s.add(new DefaultHybridEvent("a", 8, 12));
		
		d.add(s);
		
		s = new DefaultHybridEventSequence("2");
		s.add(new DefaultHybridEvent("c", 6));
		s.add(new DefaultHybridEvent("c", 8));
		s.add(new DefaultHybridEvent("b", 6, 11));
		s.add(new DefaultHybridEvent("a", 8, 11));
		
		d.add(s);
		
		s = new DefaultHybridEventSequence("3");
		s.add(new DefaultHybridEvent("c", 4));
		s.add(new DefaultHybridEvent("a", 4, 10));
		s.add(new DefaultHybridEvent("b", 4, 12));
		s.add(new DefaultHybridEvent("a", 9, 12));
		
		d.add(s);
		
		return d;
	}
}
