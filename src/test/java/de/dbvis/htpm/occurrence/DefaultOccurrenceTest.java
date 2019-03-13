package de.dbvis.htpm.occurrence;

import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.hes.events.HybridEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

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

		this.df = new DefaultOccurrence(this.seq,
				Arrays.asList(new DefaultOccurrencePoint(e2, true),
						new DefaultOccurrencePoint(e1),
						new DefaultOccurrencePoint(e2, false)));
	}
	
	@Test
	public void checkHybridEventSequence() {
		assertSame(this.seq, this.df.getHybridEventSequence());
	}
	
	/*@Test
	public void testAddAndRemove() {
		assertEquals(3, this.df.size(), 0);
		
		DefaultOccurrencePoint e1 = new DefaultOccurrencePoint(new DefaultHybridEvent("a", 10));
		
		this.df.add(e1);
		
		assertEquals(4, this.df.size(), 0);
		assertEquals(e1, this.df.get(3));
		
		
		this.df.remove(e1);
		assertEquals(3, this.df.size(), 0);
	}*/
	
	@Test
	public void createNullPointer() {
		ex.expect(NullPointerException.class);
		new DefaultOccurrence(null, Collections.emptyList());
		new DefaultOccurrence(seq, null);
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
		
		DefaultOccurrence df2 = new DefaultOccurrence(this.seq,
				Arrays.asList(new DefaultOccurrencePoint(e2, true),
						new DefaultOccurrencePoint(e1),
						new DefaultOccurrencePoint(e2, false)));

		assertNotSame(this.df, df2);
		assertEquals(this.df, df2);
		
		assertThat(this.df, not(equalTo(new DefaultOccurrence(seq2, Collections.emptyList()))));
	}
}
