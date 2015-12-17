package de.dbvis.htpm.htp.eventnodes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class OrderRelationTest {

	@Test
	public void checkStringRepresentations() {
		assertEquals("<", OrderRelation.SMALLER.toString());
		assertEquals("=", OrderRelation.EQUAL.toString());
	}
	
	@Test
	public void checkEquality() {
		assertSame(OrderRelation.EQUAL, OrderRelation.EQUAL);
		assertSame(OrderRelation.SMALLER, OrderRelation.SMALLER);
		assertThat(OrderRelation.EQUAL, not(equalTo(OrderRelation.SMALLER)));
	}
}
