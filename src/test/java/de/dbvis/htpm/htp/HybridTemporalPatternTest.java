package de.dbvis.htpm.htp;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.htp.eventnodes.*;
import de.dbvis.htpm.util.UniqueIDConverter;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static de.dbvis.htpm.htp.DefaultHybridTemporalPatternBuilder.buildFromSequence;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HybridTemporalPatternTest {
	
	private HybridTemporalPattern p;
	private HybridTemporalPattern p2;
	private HybridTemporalPattern p3;
	
	public HybridTemporalPatternTest() {
		DefaultHybridEventSequence s = new DefaultHybridEventSequence("1");
		s.add(new DefaultHybridEvent("a", 1, 2));
		s.add(new DefaultHybridEvent("b", 3));
		
		this.p = buildFromSequence(s).getPattern(); //a<b<a
		
		s = new DefaultHybridEventSequence("2");
		s.add(new DefaultHybridEvent("a", 1, 3));
		s.add(new DefaultHybridEvent("b", 2, 4));
		
		this.p2 = buildFromSequence(s).getPattern(); //a<b<a<b
		
		s = new DefaultHybridEventSequence("3");
		s.add(new DefaultHybridEvent("a", 1));
		s.add(new DefaultHybridEvent("b", 2, 4));
		
		this.p3 = buildFromSequence(s).getPattern(); //a<b<b
	}

	@Before
	public void setUp() {
		//fix id order
		UniqueIDConverter.reset();
		new PointEventNode("a");
		new PointEventNode("b");
		new PointEventNode("c");
	}

	@Test
	public void testPointEvents() {
		List<HTPItem> items = this.p.getPatternItems();
		assertTrue(items.get(0) instanceof IntervalStartEventNode);

		assertTrue(items.get(1) instanceof OrderRelation);

		assertTrue(items.get(2) instanceof IntervalEndEventNode);

		assertTrue(items.get(3) instanceof OrderRelation);

		assertTrue(items.get(4) instanceof PointEventNode);
	}
	
	@Test
	public void testPattern() {
		assertEquals("(a+0<a-0<b)", this.p.toString());
	}
	
	@Test
	public void testPattern2() {
		assertEquals("(a+0<b+0<a-0<b-0)", this.p2.toString());
	}
	
	@Test
	public void testPattern3() {
		assertEquals("(a<b+0<b-0)", this.p3.toString());
	}
	
	/* TODO: these tests are now invalid since we removed modifying method
	@Test
	public void testdeleteP1() {
		assertEquals("htp1=(a+0<a-0)", this.p.deleteLastEvent().toString());
	}
	
	@Test
	public void testdeleteP2() {
		assertEquals("htp2=(a+0<a-0)", this.p2.deleteLastEvent().toString());
	}
	
	@Test
	public void testdeleteP3() {
		assertEquals("htp3=(a)", this.p3.deleteLastEvent().toString());
	}
	*/

	@Test
	public void testInitiationByPatternRepresentation() {
		assertEquals("(a<b+0=c<b-0<b+1<b-1)",
				new DefaultHybridTemporalPattern("a<b+0=c<b-0<b+1<b-1").toString());
	}
}
