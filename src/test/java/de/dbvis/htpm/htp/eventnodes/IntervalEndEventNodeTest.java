package de.dbvis.htpm.htp.eventnodes;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IntervalEndEventNodeTest {
	
	@Rule
	public ExpectedException ex = ExpectedException.none();

	/*@Test
	public void checkCreationByHybridEvent() {
		HybridEvent h = new DefaultHybridEvent("a", 1, 3);
		IntervalEndEventNode p = new IntervalEndEventNode(h, 2);
		assertEquals(h.getEventId(), p.getEventNodeId());
		assertEquals(h.getEndPoint(), p.getTimePoint(), 0);
		assertEquals(2, p.getOccurrenceMark(), 0);
		assertSame(h, p.getHybridEvent());
		assertEquals("a-2", p.toString());
	}*/
	
	/*@Test
	public void checkWrongCreationByHybridEventNull() {
		ex.expect(NullPointerException.class);
		new IntervalEndEventNode((HybridEvent) null, 0);
	}*/
	
	/*@Test
	public void checkWrongCreationByHybridEventWrongType() {
		ex.expect(IllegalArgumentException.class);
		new IntervalEndEventNode(new DefaultHybridEvent("a", 1), 0);
	}*/
	
	@Test
	public void checkCreationById() {
		IntervalEndEventNode p = new IntervalEndEventNode("a", 3);
		assertEquals("a", p.getStringEventId());
		assertEquals(3, p.getOccurrenceMark(), 0);
		//assertNull(p.getHybridEvent());
		assertEquals("a-3", p.toString());
	}
	
	@Test
	public void checkWrongCreationByIdANull() {
		ex.expect(NullPointerException.class);
		new IntervalEndEventNode((String) null, 1);
	}
	
	@Test
	public void checkWrongCreationByIdIllegalCharacter1() {
		ex.expect(IllegalArgumentException.class);
		new IntervalEndEventNode("<", 0);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter2() {
		ex.expect(IllegalArgumentException.class);
		new IntervalEndEventNode("=", 0);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter3() {
		ex.expect(IllegalArgumentException.class);
		new IntervalEndEventNode("+", 0);
	}
	
	@Test
	public void checkWrongCreationByIdAndTimePointIllegalCharacter4() {
		ex.expect(IllegalArgumentException.class);
		new IntervalEndEventNode("-", 0);
	}
	
	@Test
	public void checkEquality() {
		IntervalEndEventNode p1 = new IntervalEndEventNode("a", 2);
		assertThat(p1, not(equalTo(new IntervalEndEventNode("b", 2)))); //different by id
		assertThat(p1, not(equalTo(new IntervalEndEventNode("a", 1)))); //different by occurrence mark
	}
}
