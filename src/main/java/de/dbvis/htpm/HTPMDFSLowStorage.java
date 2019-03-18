package de.dbvis.htpm;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.util.HTPMOutputEvent;

import java.util.ArrayList;
import java.util.Collection;
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

        List<List<PatternOccurrence>> m;

        m = this.genL1();

        this.fireHTPMEvent(new HTPMOutputEvent(this, 1, m.get(0).size(),
                m.stream().flatMap(Collection::stream).collect(
                        Collectors.toMap(po -> po.pattern, po -> po.occurrences.stream().map(link -> link.child)
                                .collect(Collectors.toList())))));

        patternDFS(m.get(0), 2);
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
                List<Map<HybridTemporalPattern, List<TreeLink>>> joined =
                        join(first.pattern.getPrefix(),
                                first.pattern, first.occurrences,
                                second.pattern, second.occurrences,
                                depth);

                //parse into pattern occurrences
                List<PatternOccurrence> parentFirst = joined.get(0).entrySet().stream()
                        .map(entry -> new PatternOccurrence(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
                List<PatternOccurrence> parentSecond = joined.get(1).entrySet().stream()
                        .map(entry -> new PatternOccurrence(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());

                partitions.get(i).addAll(parentFirst);
                partitions.get(j).addAll(parentSecond);
            }

            //release current pattern, we will not use it any more
            m.set(i, null);

            //continuously output found patterns
            fireHTPMEvent(new HTPMOutputEvent(this, depth, partitions.get(i).size(), partitions.get(i).stream()
                    .collect(Collectors.toMap(
                            po -> po.pattern,
                            po -> po.occurrences.stream().map(link -> link.child).collect(Collectors.toList())))));

            if (constraint.shouldGeneratePatternsOfLength(depth + 1)) {
                patternDFS(partitions.get(i), depth + 1);
            }
        }
    }

}
