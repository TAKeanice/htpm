package de.dbvis.htpm;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;
import de.dbvis.htpm.util.HTPMEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

        List<PatternOccurrence> m;

        m = this.genL1();

        this.patterns.add(m);

        this.fireHTPMEvent(new HTPMEvent(this, 1, m.size()));

        this.patterns.add(patternDFS(m, 2));

        System.out.println("generated a total of " + (this.patterns.get(0).size() + this.patterns.get(1).size()) + " patterns");
    }

    private List<PatternOccurrence> patternDFS(List<PatternOccurrence> m, int depth) {

        List<List<PatternOccurrence>> children = new ArrayList<>();

        for (int i = 0; i < m.size(); i++) {
            children.add(new ArrayList<>());
        }

        for (int i = 0; i < m.size(); i++) {
            PatternOccurrence first = m.get(i);
            for (int j = i; j < m.size(); j++) {
                PatternOccurrence second = m.get(j);
                Map<HybridTemporalPattern, List<Occurrence>> joined =
                        join(first.pattern.getPrefix(),
                                first.pattern, first.occurrences,
                                second.pattern, second.occurrences,
                                depth);
                List<PatternOccurrence> parentFirst = joined.entrySet().stream()
                        .filter(entry -> entry.getKey().getPrefix() == first.pattern)
                        .map(entry -> new PatternOccurrence(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
                List<PatternOccurrence> parentSecond = joined.entrySet().stream()
                        .filter(entry -> entry.getKey().getPrefix() == second.pattern)
                        .map(entry -> new PatternOccurrence(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());

                children.get(i).addAll(parentFirst);
                children.get(j).addAll(parentSecond);
            }

            final List<PatternOccurrence> collapsedChildren;
            if (constraint.shouldGeneratePatternsOfLength(depth + 1)) {
                collapsedChildren = patternDFS(children.get(i), depth + 1);
            } else {
                collapsedChildren = children.get(i);
            }
            children.set(i, collapsedChildren);
        }

        return children.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }
}
