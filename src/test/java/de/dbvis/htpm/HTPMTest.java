package de.dbvis.htpm;

import de.dbvis.htpm.constraints.*;
import de.dbvis.htpm.db.DefaultHybridEventSequenceDatabase;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.hes.DefaultHybridEventSequence;
import de.dbvis.htpm.hes.HybridEventSequence;
import de.dbvis.htpm.hes.events.DefaultHybridEvent;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.util.HTPMEvent;
import de.dbvis.htpm.util.HTPMOutputEvent;
import de.dbvis.htpm.util.HTPMOutputListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HTPMTest {
	
	@Test
	public void htpmForHybridPatternsTest() {

		HybridEventSequenceDatabase d = new DefaultHybridEventSequenceDatabase();

		HybridEventSequence s = new DefaultHybridEventSequence("1");
		
		s.add(new DefaultHybridEvent("c", 6));
		
		s.add(new DefaultHybridEvent("c", 8));
		s.add(new DefaultHybridEvent("a", 5, 10));
		s.add(new DefaultHybridEvent("b", 6, 12));
		s.add(new DefaultHybridEvent("a", 8, 12));
		
		d.add(s);
		
		s = new DefaultHybridEventSequence("2");
		s.add(new DefaultHybridEvent("c", 6));
		s.add(new DefaultHybridEvent("c", 8));
		s.add(new DefaultHybridEvent("b", 6, 11));
		s.add(new DefaultHybridEvent("a", 8, 11));
		
		d.add(s);
		
		s = new DefaultHybridEventSequence("3");
		s.add(new DefaultHybridEvent("c", 4));
		s.add(new DefaultHybridEvent("a", 4, 10));
		s.add(new DefaultHybridEvent("b", 4, 12));
		s.add(new DefaultHybridEvent("a", 9, 12));
		
		d.add(s);

		double minSupport = 0.5;
		final AgrawalSupportConstraint defaultConstraint = new AgrawalSupportConstraint(d.size(), minSupport);
		HTPM htpm = new HTPM(d, defaultConstraint);
		
		htpm.addHTPMListener(event -> System.out.println("Generation: " + event.getGeneration() + " Number of patterns: " + event.getNumberOfPatterns()));
		
		htpm.start();

		final List<Map.Entry<HybridTemporalPattern, List<Occurrence>>> patterns =
				getPatternsSortedWithFixedOccurrenceOrder(htpm.getPatternsSortedByLength());

		String expected = "[" +
				"(a+0<a-0)=[1(5.0,10.0), 1(8.0,12.0), 2(8.0,11.0), 3(4.0,10.0), 3(9.0,12.0)], " +
				"(b+0<b-0)=[1(6.0,12.0), 2(6.0,11.0), 3(4.0,12.0)], " +
				"(c)=[1(6.0), 1(8.0), 2(6.0), 2(8.0), 3(4.0)], " +
				"(a+0<a+1<a-0<a-1)=[1(5.0,8.0,10.0,12.0), 3(4.0,9.0,10.0,12.0)], " +
				"(a+0=c<a-0)=[1(8.0,8.0,12.0), 2(8.0,8.0,11.0), 3(4.0,4.0,10.0)], " +
				"(b+0<a+0<a-0=b-0)=[1(6.0,8.0,12.0,12.0), 2(6.0,8.0,11.0,11.0), 3(4.0,9.0,12.0,12.0)], " +
				"(b+0<c<b-0)=[1(6.0,8.0,12.0), 2(6.0,8.0,11.0)], " +
				"(b+0=c<b-0)=[1(6.0,6.0,12.0), 2(6.0,6.0,11.0), 3(4.0,4.0,12.0)], " +
				"(c<a+0<a-0)=[1(6.0,8.0,12.0), 2(6.0,8.0,11.0), 3(4.0,9.0,12.0)], " +
				"(c<c)=[1(6.0,8.0), 2(6.0,8.0)], " +
				"(b+0<a+0=c<a-0=b-0)=[1(6.0,8.0,8.0,12.0,12.0), 2(6.0,8.0,8.0,11.0,11.0)], " +
				"(b+0=c<a+0<a-0=b-0)=[1(6.0,6.0,8.0,12.0,12.0), 2(6.0,6.0,8.0,11.0,11.0), 3(4.0,4.0,9.0,12.0,12.0)], " +
				"(b+0=c<c<b-0)=[1(6.0,6.0,8.0,12.0), 2(6.0,6.0,8.0,11.0)], " +
				"(c<a+0=c<a-0)=[1(6.0,8.0,8.0,12.0), 2(6.0,8.0,8.0,11.0)], " +
				"(b+0=c<a+0=c<a-0=b-0)=[1(6.0,6.0,8.0,8.0,12.0,12.0), 2(6.0,6.0,8.0,8.0,11.0,11.0)]" +
				"]";

		Assert.assertEquals(expected, patterns.toString());

		//run other HTPMs to compare

		TemporalPatternProducer originalHTPM = new OriginalHTPM(d, minSupport);
		final List<Map.Entry<HybridTemporalPattern, List<Occurrence>>> patterns2 = getPatternsSortedByLength(originalHTPM);
		Assert.assertEquals(expected, patterns2.toString());

		TemporalPatternProducer fullyParallelHTPM = new HTPMFullyParallel(d, defaultConstraint, 10);
		final List<Map.Entry<HybridTemporalPattern, List<Occurrence>>> patterns3 = getPatternsSortedByLength(fullyParallelHTPM);
		Assert.assertEquals(expected, patterns3.toString());

		//run with subpattern constraint

		String subpattern = "b+0<a+0<a-0=b-0";
		SubPatternConstraint sbp = new SubPatternConstraint(subpattern);
		CMAPConstraint cmap = new CMAPConstraint(d, defaultConstraint);
		ConstraintCollection constraintCollection = new ConstraintCollection(Arrays.asList(defaultConstraint, sbp, cmap));

		htpm = new HTPM(d, constraintCollection);
		htpm.start();

		final List<Map.Entry<HybridTemporalPattern, List<Occurrence>>> patterns4 = getPatternsSortedWithFixedOccurrenceOrder(htpm.getPatternsSortedByLength());
		Assert.assertEquals("[" +
						"(b+0<a+0<a-0=b-0)=[1(6.0,8.0,12.0,12.0), 2(6.0,8.0,11.0,11.0), 3(4.0,9.0,12.0,12.0)], " +
						"(b+0<a+0=c<a-0=b-0)=[1(6.0,8.0,8.0,12.0,12.0), 2(6.0,8.0,8.0,11.0,11.0)], " +
						"(b+0=c<a+0<a-0=b-0)=[1(6.0,6.0,8.0,12.0,12.0), 2(6.0,6.0,8.0,11.0,11.0), 3(4.0,4.0,9.0,12.0,12.0)], " +
						"(b+0=c<a+0=c<a-0=b-0)=[1(6.0,6.0,8.0,8.0,12.0,12.0), 2(6.0,6.0,8.0,8.0,11.0,11.0)]]",
				patterns4.toString());


	}

	private List<Map.Entry<HybridTemporalPattern, List<Occurrence>>> getPatternsSortedByLength(TemporalPatternProducer htpm) {
		List<Stream<HTPMOutputEvent.PatternOccurrence>> output = new ArrayList<>();
		HTPMOutputListener listener = createAccumulatingListener(output);
		htpm.addHTPMListener(listener);
		htpm.start();
		Set<HTPMOutputEvent.PatternOccurrence> patternOccurrenceSet = patternOccurrencesFromAccumulatedStreams(output);

		TreeMap<HybridTemporalPattern, Set<Occurrence>> hybridTemporalPatternListTreeMap = new TreeMap<>(HybridTemporalPattern::compareTo);

		hybridTemporalPatternListTreeMap.putAll(patternOccurrenceSet.stream().collect(Collectors.toMap(po -> po.pattern, po -> po.occurrences)));

		return getPatternsSortedWithFixedOccurrenceOrder(hybridTemporalPatternListTreeMap);
	}

	private List<Map.Entry<HybridTemporalPattern, List<Occurrence>>> getPatternsSortedWithFixedOccurrenceOrder(
			Map<HybridTemporalPattern, Set<Occurrence>> patternsSortedByLength) {
		return patternsSortedByLength.entrySet()
				.stream().map((Map.Entry<HybridTemporalPattern, Set<Occurrence>> entry) ->
						Map.entry(entry.getKey(),
								entry.getValue().stream()
										.sorted(Comparator
												.comparing((Occurrence occ) -> Integer.parseInt(occ.getHybridEventSequence().getSequenceId()))
												.thenComparing((Occurrence occ) -> occ.get(0).getTimePoint())).collect(Collectors.toList())))
				.collect(Collectors.toList());
	}

	@Test
	public void htpmAsSequentialMinerTest() {
		HybridEventSequenceDatabase d = new DefaultHybridEventSequenceDatabase();

		// from https://fenix.tecnico.ulisboa.pt/downloadFile/1407993358847907/licao_8.pdf slide 38

		DefaultHybridEventSequence s1 = new DefaultHybridEventSequence("1");
		s1.add(new DefaultHybridEvent("a", 0));
		s1.add(new DefaultHybridEvent("a", 1));
		s1.add(new DefaultHybridEvent("b", 1));
		s1.add(new DefaultHybridEvent("c", 1));
		s1.add(new DefaultHybridEvent("a", 2));
		s1.add(new DefaultHybridEvent("c", 2));
		s1.add(new DefaultHybridEvent("d", 3));
		s1.add(new DefaultHybridEvent("c", 4));
		s1.add(new DefaultHybridEvent("f", 4));
		d.add(s1);

		DefaultHybridEventSequence s2 = new DefaultHybridEventSequence("2");
		s2.add(new DefaultHybridEvent("a", 0));
		s2.add(new DefaultHybridEvent("d", 0));
		s2.add(new DefaultHybridEvent("c", 1));
		s2.add(new DefaultHybridEvent("b", 2));
		s2.add(new DefaultHybridEvent("c", 2));
		s2.add(new DefaultHybridEvent("a", 3));
		s2.add(new DefaultHybridEvent("e", 3));
		d.add(s2);

		DefaultHybridEventSequence s3 = new DefaultHybridEventSequence("3");
		s3.add(new DefaultHybridEvent("e", 0));
		s3.add(new DefaultHybridEvent("f", 0));
		s3.add(new DefaultHybridEvent("a", 1));
		s3.add(new DefaultHybridEvent("b", 1));
		s3.add(new DefaultHybridEvent("d", 2));
		s3.add(new DefaultHybridEvent("f", 2));
		s3.add(new DefaultHybridEvent("c", 3));
		s3.add(new DefaultHybridEvent("b", 4));
		d.add(s3);

		DefaultHybridEventSequence s4 = new DefaultHybridEventSequence("4");
		s4.add(new DefaultHybridEvent("e", 1));
		s4.add(new DefaultHybridEvent("g", 2));
		s4.add(new DefaultHybridEvent("a", 3));
		s4.add(new DefaultHybridEvent("f", 3));
		s4.add(new DefaultHybridEvent("c", 4));
		s4.add(new DefaultHybridEvent("b", 5));
		s4.add(new DefaultHybridEvent("c", 6));
		d.add(s4);

		Set<HybridTemporalPattern> correctResults = new HashSet<>(Arrays.asList(
				new DefaultHybridTemporalPattern("a"),
				new DefaultHybridTemporalPattern("a<a"),
				new DefaultHybridTemporalPattern("a<b"),
				new DefaultHybridTemporalPattern("a<b=c"),
				new DefaultHybridTemporalPattern("a<b=c<a"),
				new DefaultHybridTemporalPattern("a<b<a"),
				new DefaultHybridTemporalPattern("a<b<c"),
				new DefaultHybridTemporalPattern("a=b"),
				new DefaultHybridTemporalPattern("a=b<c"),
				new DefaultHybridTemporalPattern("a=b<d"),
				new DefaultHybridTemporalPattern("a=b<f"),
				new DefaultHybridTemporalPattern("a=b<d<c"),
				new DefaultHybridTemporalPattern("a<c"),
				new DefaultHybridTemporalPattern("a<c<a"),
				new DefaultHybridTemporalPattern("a<c<b"),
				new DefaultHybridTemporalPattern("a<c<c"),
				new DefaultHybridTemporalPattern("a<d"),
				new DefaultHybridTemporalPattern("a<d<c"),
				new DefaultHybridTemporalPattern("a<f"),
				new DefaultHybridTemporalPattern("b"),
				new DefaultHybridTemporalPattern("b<a"),
				new DefaultHybridTemporalPattern("b<c"),
				new DefaultHybridTemporalPattern("b=c"),
				new DefaultHybridTemporalPattern("b=c<a"),
				new DefaultHybridTemporalPattern("b<d"),
				new DefaultHybridTemporalPattern("b<d<c"),
				new DefaultHybridTemporalPattern("b<f"),
				new DefaultHybridTemporalPattern("c"),
				new DefaultHybridTemporalPattern("c<a"),
				new DefaultHybridTemporalPattern("c<b"),
				new DefaultHybridTemporalPattern("c<c"),
				new DefaultHybridTemporalPattern("d"),
				new DefaultHybridTemporalPattern("d<b"),
				new DefaultHybridTemporalPattern("d<c"),
				new DefaultHybridTemporalPattern("d<c<b"),
				new DefaultHybridTemporalPattern("e"),
				new DefaultHybridTemporalPattern("e<a"),
				new DefaultHybridTemporalPattern("e<a<b"),
				new DefaultHybridTemporalPattern("e<a<c"),
				new DefaultHybridTemporalPattern("e<a<c<b"),
				new DefaultHybridTemporalPattern("e<b"),
				new DefaultHybridTemporalPattern("e<b<c"),
				new DefaultHybridTemporalPattern("e<c"),
				new DefaultHybridTemporalPattern("e<c<b"),
				new DefaultHybridTemporalPattern("e<f"),
				new DefaultHybridTemporalPattern("e<f<b"),
				new DefaultHybridTemporalPattern("e<f<c"),
				new DefaultHybridTemporalPattern("e<f<c<b"),
				new DefaultHybridTemporalPattern("f"),
				new DefaultHybridTemporalPattern("f<b"),
				new DefaultHybridTemporalPattern("f<b<c"),
				new DefaultHybridTemporalPattern("f<c"),
				new DefaultHybridTemporalPattern("f<c<b")
		));


		double minsupport = 0.5;

		final ArrayList<HybridTemporalPattern> expected = new ArrayList<>(correctResults);
		expected.sort(HybridTemporalPattern::compareTo);

		boolean[] options = new boolean[]{false, true};

		for (boolean fullyParallel : options) {
			for (boolean dfs : options) {
				for (boolean lowStorage : options) {
					for (boolean cmap : options) {
						List<HybridTemporalPattern> algorithmResults = new ArrayList<>(runHTPM(d, minsupport, dfs, !dfs && fullyParallel, lowStorage, cmap));
						algorithmResults.sort(HybridTemporalPattern::compareTo);
						Assert.assertEquals(expected, algorithmResults);
					}
				}
			}
		}

		OriginalHTPM htpm = new OriginalHTPM(d, minsupport);
		List<HybridTemporalPattern> algorithmResults = new ArrayList<>(runHTPM(htpm));
		algorithmResults.sort(HybridTemporalPattern::compareTo);
		Assert.assertEquals(expected, algorithmResults);
	}

	@Test
	public void htpmAsItemsetMinerTest() {
		HybridEventSequenceDatabase d = new DefaultHybridEventSequenceDatabase();

		HybridEventSequence s1 = new DefaultHybridEventSequence("s1");
		s1.add(new DefaultHybridEvent("A", 0.0));
		s1.add(new DefaultHybridEvent("B", 0.0));
		s1.add(new DefaultHybridEvent("C", 0.0));
		HybridEventSequence s2 = new DefaultHybridEventSequence("s2");
		s2.add(new DefaultHybridEvent("A", 0.0));
		s2.add(new DefaultHybridEvent("C", 0.0));
		s2.add(new DefaultHybridEvent("D", 0.0));
		HybridEventSequence s3 = new DefaultHybridEventSequence("s3");
		s3.add(new DefaultHybridEvent("C", 0.0));
		s3.add(new DefaultHybridEvent("D", 0.0));
		s3.add(new DefaultHybridEvent("E", 0.0));
		HybridEventSequence s4 = new DefaultHybridEventSequence("s4");
		s4.add(new DefaultHybridEvent("B", 0.0));
		s4.add(new DefaultHybridEvent("C", 0.0));
		s4.add(new DefaultHybridEvent("D", 0.0));
		HybridEventSequence s5 = new DefaultHybridEventSequence("s5");
		s5.add(new DefaultHybridEvent("B", 0.0));
		s5.add(new DefaultHybridEvent("D", 0.0));
		s5.add(new DefaultHybridEvent("E", 0.0));
		HybridEventSequence s6 = new DefaultHybridEventSequence("s6");
		s6.add(new DefaultHybridEvent("A", 0.0));
		s6.add(new DefaultHybridEvent("B", 0.0));
		s6.add(new DefaultHybridEvent("F", 0.0));
		HybridEventSequence s7 = new DefaultHybridEventSequence("s7");
		s7.add(new DefaultHybridEvent("A", 0.0));
		s7.add(new DefaultHybridEvent("B", 0.0));
		s7.add(new DefaultHybridEvent("D", 0.0));
		HybridEventSequence s8 = new DefaultHybridEventSequence("s8");
		s8.add(new DefaultHybridEvent("A", 0.0));
		s8.add(new DefaultHybridEvent("C", 0.0));
		s8.add(new DefaultHybridEvent("D", 0.0));
		HybridEventSequence s9 = new DefaultHybridEventSequence("s9");
		s9.add(new DefaultHybridEvent("D", 0.0));
		s9.add(new DefaultHybridEvent("E", 0.0));
		s9.add(new DefaultHybridEvent("F", 0.0));

		d.add(s1);
		d.add(s2);
		d.add(s3);
		d.add(s4);
		d.add(s5);
		d.add(s6);
		d.add(s7);
		d.add(s8);
		d.add(s9);

		final Set<HybridTemporalPattern> output1Patterns = 	runHTPM(d, 0.2, false, false, false, false);

		boolean[] options = new boolean[]{false, true};

		for (boolean dfs : options) {
			for (boolean lowStorage : options) {
				for (boolean cmap : options) {
					Assert.assertEquals(output1Patterns, runHTPM(d, 0.2, dfs, false, lowStorage, cmap));
				}
			}
		}
	}

	private Set<HybridTemporalPattern> patternsFromAccumulatedStreams(List<Stream<HTPMOutputEvent.PatternOccurrence>> outputStreams) {
		return outputStreams.stream().flatMap(s -> s.map(po -> po.pattern)).collect(Collectors.toSet());
	}

	private Set<HTPMOutputEvent.PatternOccurrence> patternOccurrencesFromAccumulatedStreams(List<Stream<HTPMOutputEvent.PatternOccurrence>> outputStreams) {
		return outputStreams.stream().flatMap(s -> s).collect(Collectors.toSet());
	}

	private HTPMOutputListener createAccumulatingListener(List<Stream<HTPMOutputEvent.PatternOccurrence>> streams) {
		return new HTPMOutputListener() {
			@Override
			public void outputGenerated(HTPMOutputEvent event) {
				synchronized (this) { streams.add(event.getPatternOccurrenceStream()); }
			}

			@Override
			public void generationCalculated(HTPMEvent event) {
			}
		};
	}

	private Set<HybridTemporalPattern> runHTPM(HybridEventSequenceDatabase database,
											   double minSupport, boolean dfs, boolean fullyParallel, boolean lowStorage, boolean cmap) {

		//plain (no episode mining) support threshold
		//activate cmap

		AgrawalSupportConstraint defaultConstraint;
		CMAPConstraint cmapConstraint = null;

		final List<HTPMConstraint> constraints = new ArrayList<>();

		defaultConstraint = new AgrawalSupportConstraint(database.size(), minSupport);
		constraints.add(defaultConstraint);
		if (cmap) {
			ConstraintCollection constraintsForSubpatterns = new ConstraintCollection(new ArrayList<>(constraints));
			cmapConstraint = new CMAPConstraint(database, constraintsForSubpatterns);
			constraints.add(cmapConstraint);
		}


		final ConstraintCollection combinedConstraint = new ConstraintCollection(constraints);

		double startTime = System.currentTimeMillis();


		HTPM htpm;
		if (dfs) {
			if (lowStorage) {
				htpm = new HTPMDFSLowStorage(database, combinedConstraint);
			} else {
				htpm = new HTPMDFS(database, combinedConstraint);
			}
		} else if (fullyParallel) {
			htpm = new HTPMFullyParallel(database, combinedConstraint, 10);
		} else {
			htpm = new HTPM(database, combinedConstraint, lowStorage, 1);
		}

		Set<HybridTemporalPattern> output = runHTPM(htpm);

		System.out.println("duration: " + (System.currentTimeMillis() - startTime));

		System.out.println("discarded patterns by default constraint: " + defaultConstraint.getPatternsDiscardedCount());
		System.out.println("discarded occurrences by default constraint: " + defaultConstraint.getOccurrencesDiscardedCount());
		if (cmap && !dfs) {
			System.out.println("prevented joins by CMAP constraint: " + cmapConstraint.getPatternJoinPreventedCount());
		}

		return output;
	}

	private Set<HybridTemporalPattern> runHTPM(TemporalPatternProducer htpm) {
		List<Stream<HTPMOutputEvent.PatternOccurrence>> output = new ArrayList<>();
		HTPMOutputListener listener = createAccumulatingListener(output);
		htpm.addHTPMListener(listener);
		htpm.start();
		return patternsFromAccumulatedStreams(output);
	}
}
