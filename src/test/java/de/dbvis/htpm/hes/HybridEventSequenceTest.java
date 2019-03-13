package de.dbvis.htpm.hes;

import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.DefaultOccurrencePoint;
import de.dbvis.htpm.occurrence.Occurrence;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public final class HybridEventSequenceTest {

	@Rule
	public ExpectedException ex = ExpectedException.none();
	
	private HybridEventSequence seq;
	
	@Before
	public void setUp() {
		this.seq = new DefaultHybridEventSequence("test");
		
		this.seq.add(new DefaultHybridEvent("a", 1));
		
		this.seq.add(new DefaultHybridEvent("b", 1, 4));
		
		this.seq.add(new DefaultHybridEvent("a", 2));
		
		this.seq.add(new DefaultHybridEvent("b", 3, 6));
	}
	
	@Test
	public void testWrongInstantiation() {
		ex.expect(NullPointerException.class);
		new DefaultHybridEventSequence(null);
	}
	
	@Test
	public void testAddRemove() {
		assertEquals("test", this.seq.getSequenceId());
		
		//current state
		assertEquals(4, this.seq.getEvents().size());
		
		assertEquals(new DefaultHybridEvent("a", 2), this.seq.getEvents().get(2));
		
		this.seq.add(new DefaultHybridEvent("remove", 0));
		
		assertEquals(5, this.seq.getEvents().size());
		
		this.seq.remove(new DefaultHybridEvent("remove", 0));
		
		assertEquals(4, this.seq.getEvents().size());
	}
	
	@Test
	public void testRepresentation() {
		assertEquals("test=(a,(1.0));(b,(1.0,4.0));(a,(2.0));(b,(3.0,6.0))", this.seq.toString());
	}
	
	@Test
	public void testOccurByIdWithPointEvent() {
		List<Occurrence> act = this.seq.occur("a");
		
		//expected to have two occurrences, each of the occurrences has
		//one PointEventNode
		List<Occurrence> exp = new ArrayList<>();
		
		DefaultOccurrence oc = new DefaultOccurrence(this.seq,
				Collections.singletonList(new DefaultOccurrencePoint(this.seq.getEvents().get(0))));
		exp.add(oc);
		
		oc = new DefaultOccurrence(this.seq,
				Collections.singletonList(new DefaultOccurrencePoint(this.seq.getEvents().get(2))));
		exp.add(oc);
		
		assertEquals(exp, act);
		
		//the occurrencepoint actually holds the hybrid event
		assertEquals(new DefaultHybridEvent("a", 1), act.get(0).get(0).getHybridEvent());
	}
	
	@Test
	public void testOccurByIdWithIntervalEvent() {
		//test the same with the intervalevents
		List<Occurrence> act = this.seq.occur("b");
				
		//expected to have two occurrences, each of the occurrences has
		//one PointEventNode
		List<Occurrence>exp = new ArrayList<>();
		
		DefaultOccurrence oc = new DefaultOccurrence(this.seq,
				Arrays.asList(new DefaultOccurrencePoint(this.seq.getEvents().get(1), true),
						new DefaultOccurrencePoint(this.seq.getEvents().get(1), false)));
		exp.add(oc);
		
		oc = new DefaultOccurrence(this.seq,
				Arrays.asList(new DefaultOccurrencePoint(this.seq.getEvents().get(3), true),
						new DefaultOccurrencePoint(this.seq.getEvents().get(3), false)));
		exp.add(oc);
		
		assertEquals(exp, act);
		
		//the occurrencepoint actually holds the hybrid event
		assertEquals(new DefaultHybridEvent("b", 1, 4), act.get(0).get(0).getHybridEvent());
	}
	
	@Test
	public void testOccurByWrongId() {
		assertNotNull(this.seq.occur("c"));
		assertTrue(this.seq.occur("c").isEmpty());
	}
	
	@Test
	public void testOccurByHybridEvent() {
		assertEquals(this.seq.occur("a"), this.seq.occur(new DefaultHybridEvent("a", 0)));
		assertEquals(this.seq.occur("b"), this.seq.occur(new DefaultHybridEvent("b", 0, 1)));
		assertEquals(this.seq.occur("c"), this.seq.occur(new DefaultHybridEvent("c", 0, 1)));
	}
	
	@Test
	public void testOccurByHybridTemporalPattern() {
		assertEquals(this.seq.occur("a"), this.seq.occur(new DefaultHybridTemporalPattern("a")));
		assertEquals(this.seq.occur("b"), this.seq.occur(new DefaultHybridTemporalPattern("b+0<b-0")));
		assertEquals(this.seq.occur("c"), this.seq.occur(new DefaultHybridTemporalPattern("c")));
		assertEquals("[test(1.0,1.0,4.0), test(1.0,3.0,6.0)]", this.seq.occur(new DefaultHybridTemporalPattern("a=b+0<b-0")).toString());
		assertEquals("[test(1.0,1.0,2.0,3.0,4.0,6.0)]", this.seq.occur(new DefaultHybridTemporalPattern("a=b+0<a<b+1<b-0<b-1")).toString());
	}

	@Test
	public void testOccurWithOccurrence() {
		HybridEventSequence seq = new DefaultHybridEventSequence("1");
		HybridEvent ev = new DefaultHybridEvent("c", 6);
		seq.add(ev);

		DefaultOccurrence o = new DefaultOccurrence(seq, Collections.singletonList(new DefaultOccurrencePoint(ev)));

		assertTrue(seq.isValid(o));

		o = new DefaultOccurrence(seq, Arrays.asList(new DefaultOccurrencePoint(ev), new DefaultOccurrencePoint(ev)));

		assertFalse(seq.isValid(o));
	}
}
