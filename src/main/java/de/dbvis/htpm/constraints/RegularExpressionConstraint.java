package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.HTPUtils;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.OrderRelation;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * This is a very simple implementation of a constraint that can filter on the pattern structure.
 * During algorithm execution, the part of the pattern that does not change any more is matched against the constraint.
 * If there is no possibility for the pattern to match the full regex during future execution, the pattern is pruned.
 *
 * This could be made more effective by knowing the parts of the regex better.
 * For example, if the constraint requires a certain eventtype, but there is no hope to get a match for it in the future,
 * we could prune that branch of the search space (e.g. if that pattern is not available any more in the local group).
 * On a database-projection algorithm approach, it would be possible to accomplish this by looking ahead into prefix-projected db.
 */
public class RegularExpressionConstraint extends AcceptAllConstraint {

    private final Pattern expression;
    private final boolean prefixSelective;

    private int patternsDiscardedCount = 0;

    /**
     * Creates regular expression constraint with user-supplied pattern (must be a java regex)
     *
     * @param expression the regex. Must be valid.
     * @param prefixSelective whether the regex poses constraints on the pattern prefix.
     *                        If yes, additional performance-intensive filtering is activated,
     *                        which pays off if enough patterns are pruned.
     */
    public RegularExpressionConstraint(String expression, boolean prefixSelective) {
        this.expression = Pattern.compile(expression);
        this.prefixSelective = prefixSelective;
    }

    @Override
    public boolean patternFulfillsConstraints(HybridTemporalPattern p, List<Occurrence> occurrences, int k) {
        //if regular expression is not considered selective on prefix, we skip matching.
        final boolean passes = (!prefixSelective) || prefixMatched(p);
        if (!passes) {
            patternsDiscardedCount++;
        }
        return passes;
    }

    private boolean prefixMatched(HybridTemporalPattern p) {
        int unmodifiableBoundary = HTPUtils.getLastIndexOfUnmodifiablePart(p.getEventNodes(), new ArrayList<>());
        //since this boundary can refer to any itemset order, we need to track back a little more (to last group)
        while (unmodifiableBoundary > 0 && p.getOrderRelations().get(unmodifiableBoundary - 1) == OrderRelation.EQUAL) {
            unmodifiableBoundary--;
        }
        String testedPrefix = p.partialPatternStr(IntStream.range(0, unmodifiableBoundary).toArray());
        return expression.matcher(testedPrefix).hitEnd();
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
