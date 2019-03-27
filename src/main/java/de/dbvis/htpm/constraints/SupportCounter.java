package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.List;

public interface SupportCounter {
    /**
     * @param p the pattern in question
     * @param occurrences the occurrences of the pattern
     * @param k length of the pattern
     * @return whether the support counting method accepts that pattern
     */
    boolean isSupported(final HybridTemporalPattern p, final List<Occurrence> occurrences, int k);
}
