package de.dbvis.htpm.util;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;
import java.util.Map;

public class HTPMOutputEvent extends HTPMEvent {

    private final Map<HybridTemporalPattern, List<Occurrence>> patterns;

    public HTPMOutputEvent(Object source, int generation, int number_of_patterns, long when,
                           Map<HybridTemporalPattern, List<Occurrence>> patterns) {
        super(source, generation, number_of_patterns, when);
        this.patterns = patterns;
    }

    public HTPMOutputEvent(Object source, int generation, int number_of_patterns,
                           Map<HybridTemporalPattern, List<Occurrence>> patterns) {
        super(source, generation, number_of_patterns);
        this.patterns = patterns;
    }

    public Map<HybridTemporalPattern, List<Occurrence>> getPatterns() {
        return patterns;
    }
}
