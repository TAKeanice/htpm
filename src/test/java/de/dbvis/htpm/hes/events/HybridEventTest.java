package de.dbvis.htpm.hes.events;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.hes.events.HybridEvent;

public class HybridEventTest {
	@Rule
	public ExpectedException ex = ExpectedException.none();
	
	private HybridEvent e1 = new DefaultHybridEvent("a", 2);
	private HybridEvent e2 = new DefaultHybridEvent("b", 2, 4);
	
	@Test
	public void testNullPointerConstructor() {
		ex.expect(NullPointerException.class);
		new DefaultHybridEvent(null, 0);
	}
	
	@Test
	public void testInvalidArgumentConstructor1() {
		ex.expect(IllegalArgumentException.class);
		new DefaultHybridEvent("<", 0);
	}
	
	@Test
	public void testInvalidArgumentConstructor2() {
		ex.expect(IllegalArgumentException.class);
		new DefaultHybridEvent("=", 0);
	}
	
	@Test
	public void testInvalidArgumentConstructor3() {
		ex.expect(IllegalArgumentException.class);
		new DefaultHybridEvent("+", 0);
	}
	
	@Test
	public void testInvalidArgumentConstructor4() {
		ex.expect(IllegalArgumentException.class);
		new DefaultHybridEvent("-", 0);
	}
	
	@Test
	public void testValidArgumentConstructor() {
		new DefaultHybridEvent("1", 1);
	}
	
	@Test
	public void testFields() {
		assertEquals("a", e1.getEventId());
		assertEquals("b", e2.getEventId());
		assertEquals(2.0, e1.getTimePoint(), 0.d);
		assertEquals(2.0, e1.getStartPoint(), 0.d);
		assertNull(e1.getEndPoint());
		assertEquals(2.0, e2.getTimePoint(), 0.d);
		assertEquals(2.0, e2.getStartPoint(), 0.d);
		assertEquals(4.0, e2.getEndPoint(), 0.d);
	}
	
	@Test
	public void testToString() {
		assertEquals("(a,(2.0))", e1.toString());
		assertEquals("(b,(2.0,4.0))", e2.toString());
	}
	
	@Test
	public void testPointEvent() {
		assertTrue(e1.isPointEvent());
		assertFalse(e2.isPointEvent());
		assertNull(e1.getEndPoint());
	}
	
	@Test
	public void testEquality() {
		assertEquals(e1, e1);
		assertEquals(new DefaultHybridEvent("a", 2), e1);
		assertEquals(e2, e2);
		assertEquals(new DefaultHybridEvent("b", 2, 4), e2);
		assertThat(e1, not(equalTo(e2)));
	}
}
