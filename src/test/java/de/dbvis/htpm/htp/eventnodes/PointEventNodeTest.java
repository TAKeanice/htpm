package de.dbvis.htpm.htp.eventnodes;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PointEventNodeTest {
	
	@Rule
	public ExpectedException ex = ExpectedException.none();

	/*@Test
	public void checkCreationByHybridEvent() {
		HybridEvent h = new DefaultHybridEvent("a", 1);
		PointEventNode p = new PointEventNode(h);
		assertEquals(h.getEventId(), p.getEventNodeId());
		assertEquals(h.getTimePoint(), p.getTimePoint(), 0);
		assertSame(h, p.getHybridEvent());
		assertEquals("a", p.toString());
		assertTrue(p.isPointEvent());
		assertFalse(p.isEndEvent());
	}*/
	
	/*@Test
	public void checkWrongCreationByHybridEventNull() {
		ex.expect(NullPointerException.class);
		new PointEventNode((HybridEvent) null);
	}*/
	
	/*@Test
	public void checkWrongCreationByHybridEventWrongType() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode(new DefaultHybridEvent("a", 1, 2));
	}*/
	
	@Test
	public void checkCreationById() {
		PointEventNode p = new PointEventNode("a");
		assertEquals("a", p.getStringEventId());
		//assertNull(p.getHybridEvent());
		assertEquals("a", p.toString());
		//assertTrue(p.isPointEvent());
		//assertFalse(p.isEndEvent());
	}
	
	@Test
	public void checkWrongCreationByIdNull() {
		ex.expect(NullPointerException.class);
		new PointEventNode((String) null);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter1() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode("<");
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter2() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode("=");
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter3() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode("+");
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter4() {
		ex.expect(IllegalArgumentException.class);
		new PointEventNode("-");
	}
	
	@Test
	public void checkEquality() {
		PointEventNode p1 = new PointEventNode("a");
		assertThat(p1, not(equalTo(new PointEventNode("b"))));
	}
}
