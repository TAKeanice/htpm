package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.Set;

public interface SupportBasedConstraint {
    double getSupport(HybridTemporalPattern p, Set<Occurrence> occurrences);
}
