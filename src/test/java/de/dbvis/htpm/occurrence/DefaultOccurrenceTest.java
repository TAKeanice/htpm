package de.dbvis.htpm.occurrence;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.htp.eventnodes.IntervalEndEventNode;
import de.dbvis.htpm.htp.eventnodes.IntervalStartEventNode;
import de.dbvis.htpm.htp.eventnodes.PointEventNode;

public class DefaultOccurrenceTest {
	
	@Rule
	public ExpectedException ex = ExpectedException.none();
	
	private DefaultOccurrence df;
	private HybridEventSequence seq;
	
	@Before
	public void prepare() {
		HybridEvent e1 = new DefaultHybridEvent("a", 2);
		HybridEvent e2 = new DefaultHybridEvent("b", 1, 3);
		
		this.seq = new DefaultHybridEventSequence("seq");
		this.seq.add(e1);
		this.seq.add(e2);
		
		this.df = new DefaultOccurrence(this.seq);
		
		this.df.add(new IntervalStartEventNode(e2, 0));
		this.df.add(new PointEventNode(e1));
		this.df.add(new IntervalEndEventNode(e2, 0));
	}
	
	@Test
	public void checkHybridEventSequence() {
		assertSame(this.seq, this.df.getHybridEventSequence());
	}
	
	@Test
	public void testAddAndRemove() {
		assertEquals(3, this.df.size(), 0);
		
		PointEventNode e1 = new PointEventNode("a", 4);
		
		this.df.add(e1);
		
		assertEquals(4, this.df.size(), 0);
		assertEquals(e1, this.df.get(3));
		
		
		this.df.remove(e1);
		assertEquals(3, this.df.size(), 0);
	}
	
	@Test
	public void createNullPointer() {
		ex.expect(NullPointerException.class);
		new DefaultOccurrence(null);
	}
	
	@Test
	public void checkStringRepresentation() {
		assertEquals("seq(1.0,2.0,3.0)", this.df.toString());
	}
	
	@Test
	public void checkEquality() {
		HybridEvent e1 = new DefaultHybridEvent("a", 2);
		HybridEvent e2 = new DefaultHybridEvent("b", 1, 3);
		
		HybridEventSequence seq2 = new DefaultHybridEventSequence("seq");
		seq2.add(e1);
		seq2.add(e2);
		
		DefaultOccurrence df2 = new DefaultOccurrence(this.seq);
		
		df2.add(new IntervalStartEventNode(e2, 0));
		df2.add(new PointEventNode(e1));
		df2.add(new IntervalEndEventNode(e2, 0));
		
		assertFalse(this.df == df2);
		assertEquals(this.df, df2);
		
		assertThat(this.df, not(equalTo(new DefaultOccurrence(seq2))));
		
		df2.remove(new PointEventNode(e1));
		
		assertThat(this.df, not(equalTo(df2)));
	}
}
