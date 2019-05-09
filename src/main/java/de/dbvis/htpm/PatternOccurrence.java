package de.dbvis.htpm;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.ArrayList;
import java.util.List;

public class PatternOccurrence {
    /**
     * The canonical parent, having the same events with the same order for the first length-1 events
     * The first length-1 events are determined by the first length-1 interval startpoints or points
     * ordered by time then id then type (point/intervalstart) and finally occurrence mark order
     */
    public final HybridTemporalPattern prefix;
    public final HybridTemporalPattern pattern;
    public final List<OccurrenceTreeLink> occurrences;

    PatternOccurrence(HybridTemporalPattern prefix, HybridTemporalPattern pattern, int initialListSize) {
        this.prefix = prefix;
        this.pattern = pattern;
        this.occurrences = new ArrayList<>(initialListSize);
    }

    PatternOccurrence(HybridTemporalPattern prefix, HybridTemporalPattern pattern, List<OccurrenceTreeLink> occurrences) {
        this.prefix = prefix;
        this.pattern = pattern;
        this.occurrences = occurrences;
    }

    public static class OccurrenceTreeLink {
        /**
         * Holds the canonical parent relation for this occurrence.
         * The canonical parent must be from the same sequence,
         * and have the same occurrences for the prefix nodes as its child occurrence.
         * Prefix thereby refers to the prefix of the pattern, which the occurrence is associated to.
         */
        public final Occurrence parent;
        public final Occurrence child;

        OccurrenceTreeLink(Occurrence parent, Occurrence child) {
            this.parent = parent;
            this.child = child;
        }
    }
}
