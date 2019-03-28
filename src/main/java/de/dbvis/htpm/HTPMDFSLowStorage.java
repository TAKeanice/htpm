package de.dbvis.htpm;

import de.dbvis.htpm.constraints.HTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.util.HTPMOutputEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HTPMDFSLowStorage extends HTPMLowStorage {

    /**
     * Creates a new HTPM-Algorithm-Object.
     *
     * @param d          - The Database containing the series.
     * @param constraint - The constraint determining the pre- and post-joining pruning behavior.
     */
    public HTPMDFSLowStorage(HybridEventSequenceDatabase d, HTPMConstraint constraint) {
        super(d, constraint);
    }

    /**
     * The method that actually runs the algorithm.
     */
    @Override
    public void run() {
        if (!constraint.shouldGeneratePatternsOfLength(1)) {
            return;
        }

        List<PatternOccurrence> patterns = this.genL1().get(0);

        output(patterns, 1);

        patternDFS(patterns, 2);
    }

    private void patternDFS(List<PatternOccurrence> m, int depth) {

        List<List<PatternOccurrence>> partitions = new ArrayList<>();

        for (int i = 0; i < m.size(); i++) {
            partitions.add(new ArrayList<>());
        }

        for (int i = 0; i < m.size(); i++) {
            PatternOccurrence first = m.get(i);

            for (int j = i; j < m.size(); j++) {
                PatternOccurrence second = m.get(j);

                if (!constraint.patternsQualifyForJoin(first.pattern, second.pattern, depth)) {
                    continue;
                }

                List<Map<HybridTemporalPattern, PatternOccurrence>> joined =
                        join(first.prefix,
                                first.pattern, first.occurrences,
                                second.pattern, second.occurrences,
                                depth);

                //parse into pattern occurrences
                List<PatternOccurrence> parentFirst = new ArrayList<>(joined.get(0).values());
                partitions.get(i).addAll(parentFirst);

                if (i != j) {
                    List<PatternOccurrence> parentSecond = new ArrayList<>(joined.get(1).values());
                    partitions.get(j).addAll(parentSecond);
                }
            }

            //release current pattern, we will not use it any more
            // also removes it from the partitions stored by the calling subroutine
            m.set(i, null);

            //continuously output found patterns
            output(partitions.get(i), depth);

            if (constraint.shouldGeneratePatternsOfLength(depth + 1)) {
                patternDFS(partitions.get(i), depth + 1);
            }
        }
    }

}
