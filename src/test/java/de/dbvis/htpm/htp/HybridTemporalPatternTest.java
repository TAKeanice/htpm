package de.dbvis.htpm.htp;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.htp.eventnodes.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class HybridTemporalPatternTest {
	
	private HybridTemporalPattern p;
	private HybridTemporalPattern p2;
	private HybridTemporalPattern p3;
	
	public HybridTemporalPatternTest() {
		DefaultHybridEventSequence s = new DefaultHybridEventSequence("1");
		s.add(new DefaultHybridEvent("a", 1, 2));
		s.add(new DefaultHybridEvent("b", 3));
		
		this.p = new DefaultHybridTemporalPattern("1", s); //a<b<a
		
		s = new DefaultHybridEventSequence("2");
		s.add(new DefaultHybridEvent("a", 1, 3));
		s.add(new DefaultHybridEvent("b", 2, 4));
		
		this.p2 = new DefaultHybridTemporalPattern("2", s); //a<b<a<b
		
		s = new DefaultHybridEventSequence("3");
		s.add(new DefaultHybridEvent("a", 1));
		s.add(new DefaultHybridEvent("b", 2, 4));
		
		this.p3 = new DefaultHybridTemporalPattern("3", s); //a<b<b
	}

	@Test
	public void testPointEvents() {
		HTPItem[] items = this.p.getPatternItems();
		assertTrue(items[0] instanceof IntervalStartEventNode);
		assertFalse(((IntervalStartEventNode) items[0]).isPointEvent());
		assertFalse(((IntervalStartEventNode) items[0]).isEndEvent());

		assertTrue(items[1] instanceof OrderRelation);

		assertTrue(items[2] instanceof IntervalEndEventNode);
		assertFalse(((IntervalEndEventNode) items[2]).isPointEvent());
		assertTrue(((IntervalEndEventNode) items[2]).isEndEvent());

		assertTrue(items[3] instanceof OrderRelation);

		assertTrue(items[4] instanceof PointEventNode);
		assertTrue(((PointEventNode) items[4]).isPointEvent());
		assertFalse(((PointEventNode) items[4]).isEndEvent());
	}
	
	@Test
	public void testPattern() {
		assertEquals("htp1=(a+0<a-0<b)", this.p.toString());
	}
	
	@Test
	public void testPattern2() {
		assertEquals("htp2=(a+0<b+0<a-0<b-0)", this.p2.toString());
	}
	
	@Test
	public void testPattern3() {
		assertEquals("htp3=(a<b+0<b-0)", this.p3.toString());
	}
	
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

	@Test
	public void testInitiationByPatternRepresentation() {
		assertEquals("htp1=(a<b+0=c<b-0<b+1<b-1)", 
		new DefaultHybridTemporalPattern("1", "a<b+0=c<b-0<b+1<b-1").toString());
	}
}
