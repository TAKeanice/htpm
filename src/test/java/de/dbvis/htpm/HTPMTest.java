package de.dbvis.htpm;

import de.dbvis.htpm.db.DefaultHybridEventSequenceDatabase;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import org.junit.Before;
import org.junit.Test;

public class HTPMTest {
	private HybridEventSequenceDatabase d;

	@Before
	public void setUp() {
		d = new DefaultHybridEventSequenceDatabase();
		
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
	}
	
	@Test
	public void algorithmTest() {
		HTPM htpm = new HTPM(d, new DefaultHTPMConstraint(d, 0.5, Integer.MAX_VALUE));
		
		htpm.addHTPMListener(event -> System.out.println("Generation: "+event.getGeneration() + " Number of patterns: " + event.getNumberOfPatterns()));
		
		htpm.run();
		
		//assertEquals("{htp1=(c)=[1(6.0), 1(8.0), 2(6.0), 2(8.0), 3(4.0)], htp1=(b+0=c<c<b-0)=[1(6.0,6.0,8.0,12.0), 2(6.0,6.0,8.0,11.0)], htp1=(c<c)=[1(6.0,8.0), 2(6.0,8.0)], htp1=(a+0=c<a-0)=[1(8.0,8.0,12.0), 2(8.0,8.0,11.0), 3(4.0,4.0,10.0)], htp1=(b+0<a+0<a-0=b-0)=[1(6.0,8.0,12.0,12.0), 2(6.0,8.0,11.0,11.0), 3(4.0,9.0,12.0,12.0)], htp1=(a+0<a+1<a-0<a-1)=[1(5.0,8.0,10.0,12.0), 3(4.0,9.0,10.0,12.0)], htp1=(b+0<c<b-0)=[1(6.0,8.0,12.0), 2(6.0,8.0,11.0)], htp1=(c<a+0<a-0)=[1(6.0,8.0,12.0), 2(6.0,8.0,11.0), 3(4.0,9.0,12.0)], htp1=(b+0<a+0=c<a-0=b-0)=[1(6.0,8.0,8.0,12.0,12.0), 2(6.0,8.0,8.0,11.0,11.0)], htp1=(b+0=c<b-0)=[1(6.0,6.0,12.0), 2(6.0,6.0,11.0), 3(4.0,4.0,12.0)], htp1=(a+0<a-0)=[1(5.0,10.0), 1(8.0,12.0), 2(8.0,11.0), 3(4.0,10.0), 3(9.0,12.0)], htp1=(b+0=c<a+0=c<a-0=b-0)=[1(6.0,6.0,8.0,8.0,12.0,12.0), 2(6.0,6.0,8.0,8.0,11.0,11.0)], htp1=(c<a+0=c<a-0)=[1(6.0,8.0,8.0,12.0), 2(6.0,8.0,8.0,11.0)], htp1=(b+0=c<a+0<a-0=b-0)=[1(6.0,6.0,8.0,12.0,12.0), 2(6.0,6.0,8.0,11.0,11.0), 3(4.0,4.0,9.0,12.0,12.0)], htp1=(b+0<b-0)=[1(6.0,12.0), 2(6.0,11.0), 3(4.0,12.0)]}", htpm.getPatterns().toString());
	}
}
