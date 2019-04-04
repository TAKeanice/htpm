package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HTPUtils;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.OrderRelation;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class RegularExpressionConstraint extends AcceptAllConstraint {

    private final Pattern expression;

    private int patternsDiscardedCount = 0;

    public RegularExpressionConstraint(String expression) {
        this.expression = Pattern.compile(expression);
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        int unmodifiableBoundary = HTPUtils.getLastIndexOfUnmodifiablePart(p.getEventNodes(), new ArrayList<>());
        //since this boundary can refer to any itemset order, we need to track back a little more (to last group)
        while (unmodifiableBoundary > 0 && p.getOrderRelations().get(unmodifiableBoundary - 1) == OrderRelation.EQUAL) {
            unmodifiableBoundary--;
        }
        String testedPrefix = p.partialPatternStr(IntStream.range(0, unmodifiableBoundary).toArray());
        final boolean passes = expression.matcher(testedPrefix).hitEnd();
        if (!passes) {
            patternsDiscardedCount++;
        }
        return passes;
    }

    @Override
    public boolean shouldOutput(HybridTemporalPattern p, List<Occurrence> occurrences) {
        return expression.matcher(p.patternStr()).matches();
    }

    @Override
    public int getPatternJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrenceJoinPreventedCount() {
        return 0;
    }

    @Override
    public int getOccurrencesDiscardedCount() {
        return 0;
    }

    @Override
    public int getPatternsDiscardedCount() {
        return patternsDiscardedCount;
    }
}
