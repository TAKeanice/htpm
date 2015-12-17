package de.dbvis.htpm.examples.paper;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;

/**
 * This is example 2 out of the paper showing the occurrence method.
 * 
 * @author Wolfgang Jentner
 *
 */
public class Example2 {
	public static void main(String[] args) {
		HybridEventSequenceDatabase d = ExampleDatabase.getExample();
		
		System.out.println("occur(c,1)="+d.getSequence("1").occur("c"));
		System.out.println("occur(a,2)="+d.getSequence("2").occur("a"));
		System.out.println("occur(a,3)="+d.getSequence("3").occur("a"));
	}
}
