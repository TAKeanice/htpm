package de.dbvis.htpm.htp.eventnodes;

import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.hes.events.HybridEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class PointEventNodeTest {
	
	@Rule
	public ExpectedException ex = ExpectedException.none();

	@Test
	public void checkCreationByHybridEvent() {
		HybridEvent h = new DefaultHybridEvent("a", 1);
		PointEventNode p = new PointEventNode(h);
		assertEquals(h.getEventId(), p.getEventNodeId());
		assertEquals(h.getTimePoint(), p.getTimePoint(), 0);
		assertSame(h, p.getHybridEvent());
		assertEquals("a", p.toString());
		assertTrue(p.isPointEvent());
		assertFalse(p.isEndEvent());
	}
	
	@Test
	public void checkWrongCreationByHybridEventNull() {
		ex.expect(NullPointerException.class);
		new PointEventNode(null);
	}
	
	@Test
	public void checkWrongCreationByHybridEventWrongType() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode(new DefaultHybridEvent("a", 1, 2));
	}
	
	@Test
	public void checkCreationByIdAndTimePoint() {
		PointEventNode p = new PointEventNode("a", 1);
		assertEquals("a", p.getEventNodeId());
		assertEquals(1.d, p.getTimePoint(), 0);
		assertNull(p.getHybridEvent());
		assertEquals("a", p.toString());
		assertTrue(p.isPointEvent());
		assertFalse(p.isEndEvent());
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointNull() {
		ex.expect(NullPointerException.class);
		new PointEventNode(null, 1);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter1() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode("<", 1);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter2() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode("=", 1);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter3() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode("+", 1);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter4() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode("-", 1);
	}
	
	@Test
	public void checkEquality() {
		PointEventNode p1 = new PointEventNode(new DefaultHybridEvent("a", 1));
		PointEventNode p2 = new PointEventNode("a", 2);
		assertEquals(p1, p2);
		assertThat(p1, not(equalTo(new PointEventNode("b", 1))));
	}
}
