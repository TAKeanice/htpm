package de.dbvis.htpm;

import de.dbvis.htpm.constraints.HTPMConstraint;
import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        final List<PatternOccurrence> onePatterns = m.get(0);
        output(m, 1);

        this.patterns.addAll(patternDFS(onePatterns, 2));
    }

    private List<List<List<PatternOccurrence>>> patternDFS(List<PatternOccurrence> m, int depth) {

        if (!constraint.branchCanProduceResults(m)) {
            return Collections.emptyList();
        }

        List<List<List<PatternOccurrence>>> results = new ArrayList<>();

        List<List<PatternOccurrence>> partitions = new ArrayList<>();
        results.add(partitions);

        for (int i = 0; i < m.size(); i++) {
            partitions.add(new ArrayList<>());
        }

        for (int i = 0; i < m.size(); i++) {
            calculateBranch(m, depth, partitions, i);

            output(Collections.singletonList(partitions.get(i)), depth);

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

    protected void calculateBranch(List<PatternOccurrence> m, int depth,
                                 List<List<PatternOccurrence>> partitions, int index) {

        PatternOccurrence first = m.get(index);

        for (int j = index; j < m.size(); j++) {
            PatternOccurrence second = m.get(j);

            if (!constraint.patternsQualifyForJoin(first.prefix, first.pattern, second.pattern, depth)) {
                continue;
            }

            List<Map<HybridTemporalPattern, PatternOccurrence>> joined = join(first, second, depth);

            //parse into pattern occurrences

            List<PatternOccurrence> parentFirst = new ArrayList<>(joined.get(0).values());
            partitions.get(index).addAll(parentFirst);

            if (index != j) {
                List<PatternOccurrence> parentSecond = new ArrayList<>(joined.get(1).values());
                partitions.get(j).addAll(parentSecond);
            }
        }
    }
}
