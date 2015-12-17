package de.dbvis.htpm.htp.eventnodes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.hes.events.HybridEvent;

public class IntervalStartEventNodeTest {
	
	@Rule
	public ExpectedException ex = ExpectedException.none();

	@Test
	public void checkCreationByHybridEvent() {
		HybridEvent h = new DefaultHybridEvent("a", 1, 3);
		IntervalStartEventNode p = new IntervalStartEventNode(h, 2);
		assertEquals(h.getEventId(), p.getEventNodeId());
		assertEquals(h.getTimePoint(), p.getTimePoint(), 0);
		assertEquals(2, p.getOccurrenceMark(), 0);
		assertSame(h, p.getHybridEvent());
		assertEquals("a+2", p.toString());
	}
	
	@Test
	public void checkWrongCreationByHybridEventNull() {
		ex.expect(NullPointerException.class);
		new IntervalEndEventNode(null, 0);
	}
	
	@Test
	public void checkWrongCreationByHybridEventWrongType() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode(new DefaultHybridEvent("a", 1), 0);
	}
	
	@Test
	public void checkCreationByIdAndTimePoint() {
		IntervalStartEventNode p = new IntervalStartEventNode("a", 1, 3);
		assertEquals("a", p.getEventNodeId());
		assertEquals(1.d, p.getTimePoint(), 0);
		assertEquals(3, p.getOccurrenceMark(), 0);
		assertNull(p.getHybridEvent());
		assertEquals("a+3", p.toString());
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointNull() {
		ex.expect(NullPointerException.class);
		new IntervalStartEventNode(null, 1, 1);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter1() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode("<", 1, 0);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter2() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode("=", 1, 0);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter3() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode("+", 1, 0);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter4() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode("-", 1, 0);
	}
	
	@Test
	public void checkEquality() {
		IntervalStartEventNode p1 = new IntervalStartEventNode(new DefaultHybridEvent("a", 1, 3), 2);
		IntervalStartEventNode p2 = new IntervalStartEventNode("a", 1, 2);
		assertEquals(p1, p2);
		assertThat(p1, not(equalTo(new IntervalStartEventNode("b", 1, 2)))); //different by id
		assertThat(p1, not(equalTo(new IntervalStartEventNode("a", 1, 1)))); //different by occurrence mark
	}
}
