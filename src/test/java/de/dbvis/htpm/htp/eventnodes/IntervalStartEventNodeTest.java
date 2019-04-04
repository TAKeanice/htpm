package de.dbvis.htpm.htp.eventnodes;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IntervalStartEventNodeTest {
	
	@Rule
	public ExpectedException ex = ExpectedException.none();

	/*@Test
	public void checkCreationByHybridEvent() {
		HybridEvent h = new DefaultHybridEvent("a", 1, 3);
		IntervalStartEventNode p = new IntervalStartEventNode(h, 2);
		assertEquals(h.getEventId(), p.getEventNodeId());
		assertEquals(h.getTimePoint(), p.getTimePoint(), 0);
		assertEquals(2, p.getOccurrenceMark(), 0);
		assertSame(h, p.getHybridEvent());
		assertEquals("a+2", p.toString());
	}*/
	
	/*@Test
	public void checkWrongCreationByHybridEventNull() {
		ex.expect(NullPointerException.class);
		new IntervalEndEventNode((HybridEvent) null, 0);
	}*/
	
	/*@Test
	public void checkWrongCreationByHybridEventWrongType() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode(new DefaultHybridEvent("a", 1), 0);
	}*/
	
	@Test
	public void checkCreationById() {
		IntervalStartEventNode p = new IntervalStartEventNode("a", 3);
		assertEquals("a", p.getStringEventId());
		assertEquals(3, p.getOccurrenceMark());
		//assertNull(p.getHybridEvent());
		assertEquals("a+3", p.toString());
	}
	
	@Test
	public void checkWrongCreationByIdNull() {
		ex.expect(NullPointerException.class);
		new IntervalStartEventNode((String) null, 1);
	}
	
	@Test
	public void checkWrongCreationByIdIllegalCharacter1() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode("<", 0);
	}
	
	@Test
	public void checkWrongCreationByIdIllegalCharacter2() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode("=", 0);
	}
	
	@Test
	public void checkWrongCreationByIdIllegalCharacter3() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode("+", 0);
	}
	
	@Test
	public void checkWrongCreationByIdIllegalCharacter4() {
		ex.expect(IllegalArgumentException.class);
		new IntervalStartEventNode("-", 0);
	}
	
	@Test
	public void checkEquality() {
		IntervalStartEventNode p1 = new IntervalStartEventNode("a", 2);
		assertThat(p1, not(equalTo(new IntervalStartEventNode("b", 2)))); //different by id
		assertThat(p1, not(equalTo(new IntervalStartEventNode("a", 1)))); //different by occurrence mark
	}
}
