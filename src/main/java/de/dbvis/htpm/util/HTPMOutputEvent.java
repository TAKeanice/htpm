package de.dbvis.htpm.util;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;
import java.util.stream.Stream;

public class HTPMOutputEvent extends HTPMEvent {

    private final Stream<PatternOccurrence> patternOccurrenceStream;

    public HTPMOutputEvent(Object source, int generation, int number_of_patterns, long when,
                           Stream<PatternOccurrence> patternOccurrenceStream) {
        super(source, generation, number_of_patterns, when);
        this.patternOccurrenceStream = patternOccurrenceStream;
    }

    public HTPMOutputEvent(Object source, int generation, int number_of_patterns,
                           Stream<PatternOccurrence> patternOccurrenceStream) {
        super(source, generation, number_of_patterns);
        this.patternOccurrenceStream = patternOccurrenceStream;
    }

    public Stream<PatternOccurrence> getPatternOccurrenceStream() {
        return patternOccurrenceStream;
    }

    public static class PatternOccurrence {
        public final HybridTemporalPattern pattern;
        public final List<Occurrence> occurrences;

        public PatternOccurrence(HybridTemporalPattern pattern, List<Occurrence> occurrences) {
            this.pattern = pattern;
            this.occurrences = occurrences;
        }
    }
}
