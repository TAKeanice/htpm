package de.dbvis.htpm.examples.paper;

import de.dbvis.htpm.HTPM;
import de.dbvis.htpm.constraints.DefaultHTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;
import java.util.Map;

/**
 * This example shows the result of performing the HTPM on the
 * example database with a minimum support of 0.5.
 * 
 * @author Wolfgang Jentner
 *
 */
public class Table6 {
	public static void main(String[] args) {
		HybridEventSequenceDatabase db = ExampleDatabase.getExample();
		HTPM htpm = new HTPM(db, new DefaultHTPMConstraint(db, 0.5, Integer.MAX_VALUE));
		htpm.run();
		Map<HybridTemporalPattern, List<Occurrence>> m = htpm.getPatternsSortedByLength();
		for(HybridTemporalPattern p : m.keySet()) {
			System.out.println(p.toString() + " ("+db.support(p)+") : "+ m.get(p));
		}
	}
}
