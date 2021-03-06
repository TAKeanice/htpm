package de.dbvis.htpm;

import de.dbvis.htpm.constraints.HTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.*;

public class HTPMDFSLowStorage extends HTPMDFS {

    /**
     * Creates a new HTPM-Algorithm-Object.
     *
     * @param d          - The Database containing the series.
     * @param constraint - The constraint determining the pre- and post-joining pruning behavior.
     */
    public HTPMDFSLowStorage(HybridEventSequenceDatabase d, HTPMConstraint constraint) {
        super(d, constraint);
    }

    @Override
    public Map<HybridTemporalPattern, Set<Occurrence>> getPatterns() {
        throw new RuntimeException("Patterns are not saved after output in low storage mode!");
    }

    /**
     * The method that actually runs the algorithm.
     */
    @Override
    public void start() {
        if (!constraint.shouldGeneratePatternsOfLength(1)) {
            return;
        }

        List<PatternOccurrence> patterns = this.genL1().get(0);

        output(Collections.singletonList(new ArrayList<>(patterns)), 1);

        patternDFS(patterns, 2);
    }

    private void patternDFS(List<PatternOccurrence> m, int depth) {

        if (!constraint.branchCanProduceResults(m)) {
            m.clear();
            return;
        }

        List<List<PatternOccurrence>> partitions = new ArrayList<>();

        for (int i = 0; i < m.size(); i++) {
            partitions.add(new ArrayList<>());
        }

        for (int i = 0; i < m.size(); i++) {
            calculateBranch(m, depth, partitions, i);

            //release current pattern, we will not use it any more
            // also removes it from the partitions stored by the calling subroutine
            m.set(i, null);

            //continuously output found patterns
            output(Collections.singletonList(new ArrayList<>(partitions.get(i))), depth);

            if (constraint.shouldGeneratePatternsOfLength(depth + 1)) {
                patternDFS(partitions.get(i), depth + 1);
            } else {
                partitions.get(i).clear();
            }
        }
    }

}
