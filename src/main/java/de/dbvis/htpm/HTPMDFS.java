package de.dbvis.htpm;

import de.dbvis.htpm.constraints.HTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.util.HTPMEvent;

import java.util.*;
import java.util.stream.Collectors;

public class HTPMDFS extends HTPM {

    /**
     * Creates a new HTPM-Algorithm-Object.
     *
     * @param d          - The Database containing the series.
     * @param constraint - The constraint determining the pre- and post-joining pruning behavior.
     */
    public HTPMDFS(HybridEventSequenceDatabase d, HTPMConstraint constraint) {
        super(d, constraint);
    }

    /**
     * The method that actually runs the algorithm.
     */
    @Override
    public void run() {
        this.patterns = new ArrayList<>();

        if (!constraint.shouldGeneratePatternsOfLength(1)) {
            return;
        }

        List<List<PatternOccurrence>> m;

        m = this.genL1();

        this.patterns.add(m);

        this.fireHTPMEvent(new HTPMEvent(this, 1, m.get(0).size()));

        final List<List<List<PatternOccurrence>>> patternOccurrences = patternDFS(m.get(0), 2);
        this.patterns.addAll(patternOccurrences);

        System.out.println("generated a total of " + getPatterns().size() + " patterns");
    }

    private List<List<List<PatternOccurrence>>> patternDFS(List<PatternOccurrence> m, int depth) {

        List<List<List<PatternOccurrence>>> results = new ArrayList<>();

        List<List<PatternOccurrence>> partitions = new ArrayList<>();
        results.add(partitions);

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

                List<Map<HybridTemporalPattern, List<Occurrence>>> joined =
                        join(first.pattern.getPrefix(),
                                first.pattern, first.occurrences,
                                second.pattern, second.occurrences,
                                depth);

                //parse into pattern occurrences

                List<PatternOccurrence> parentFirst = joined.get(0).entrySet().stream()
                        .map(entry -> new PatternOccurrence(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
                partitions.get(i).addAll(parentFirst);

                if (i != j) {
                    List<PatternOccurrence> parentSecond = joined.get(1).entrySet().stream()
                            .map(entry -> new PatternOccurrence(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                    partitions.get(j).addAll(parentSecond);
                }
            }

            if (constraint.shouldGeneratePatternsOfLength(depth + 1)) {
                List<List<List<PatternOccurrence>>> partitionChildren = patternDFS(partitions.get(i), depth + 1);
                //add children level by level
                for (int j = 0; j < partitionChildren.size(); j++) {
                    if (j >= results.size()) {
                        //we came one level deeper
                        results.add(partitionChildren.get(j));
                    } else {
                        results.get(j).addAll(partitionChildren.get(j));
                    }
                }
            }
        }

        return results;
    }
}
