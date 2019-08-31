package de.dbvis.htpm.util;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;
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
        public final Set<Occurrence> occurrences;

        public PatternOccurrence(HybridTemporalPattern pattern, Set<Occurrence> occurrences) {
            this.pattern = pattern;
            this.occurrences = occurrences;
        }


        @Override
        public String toString() {
            return pattern.toString() + ": " + occurrences.toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(pattern.hashCode()).append(occurrences.hashCode()).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PatternOccurrence)) {
                return false;
            }
            final PatternOccurrence other = (PatternOccurrence) obj;
            return new EqualsBuilder().append(pattern, other.pattern).append(occurrences, other.occurrences).isEquals();
        }
    }
}
