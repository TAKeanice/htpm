package de.dbvis.htpm.constraints;

import de.dbvis.htpm.PatternOccurrence;
import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.*;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//Caution: subpattern constraint may find additional incorrect subpattern relations when quoted eventids are used.
public class SubPatternConstraint extends RegularExpressionConstraint {

    private final HybridTemporalPattern subPattern;
    private final List<Pattern> nodeRegexes;
    private int branchesCutCount = 0;

    public SubPatternConstraint(HybridTemporalPattern subPattern) {
        super(toRegex(subPattern), false);
        this.subPattern = subPattern;
        nodeRegexes = subPattern.getEventNodes().stream()
                .map(n -> Pattern.compile(n.getStringEventId())).collect(Collectors.toList());
    }

    public SubPatternConstraint(String subPattern) {
        this(new DefaultHybridTemporalPattern(subPattern));
    }

    private static String toRegex(HybridTemporalPattern p) {
        //language=RegExp
        String stayInGroupPlaceholder = "=([^<]*=)?";

        //group change: may start with order relation = and has a "<" somewhere,
        // afterwards may have any number of letters followed by an order relation
        //language=RegExp
        String changeGroupPlaceholder = "(=.*)?<(.*[<=])?";

        List<HTPItem> nodes = p.getPatternItemsInStringIdOrder();

        //pattern either starts with first subpattern element or with any number of letters followed by an order relation
        //language=RegExp
        StringBuilder regex = new StringBuilder("^(.*[<=])?");

        for (int i = 0; i < p.getOrderRelations().size(); i++) {
            appendNode(regex, (EventNode) nodes.get(i * 2));
            regex.append(p.getOrderRelations().get(i) == OrderRelation.EQUAL
                    ? stayInGroupPlaceholder
                    : changeGroupPlaceholder);
        }
        //add last node (and wildcard for everything that might come afterwards)
        appendNode(regex, (EventNode) nodes.get(nodes.size() - 1));

        //ends with last subpattern element or an order relation followed by any number of letters
        //language=RegExp
        regex.append("([<=].*)?$");

        return regex.toString();
    }

    private static void appendNode(StringBuilder regex, EventNode node) {
        final String stringEventId = node.getStringEventId();
        regex.append(stringEventId);
        if (node instanceof IntervalEventNode) {
            final String cleanedId = "omark" + stringEventId.replaceAll("[^A-Za-z0-9]", "X");
            if (node instanceof IntervalStartEventNode) {
                //capture occurrencemark in regex with named reference
                regex.append("\\+" + "(?<").append(cleanedId).append(((IntervalEventNode) node).getOccurrenceMark()).append(">[0-9]*)");
            } else if (node instanceof IntervalEndEventNode) {
                //backreference to start node occurrence mark
                regex.append("\\-\\k<").append(cleanedId).append(((IntervalEventNode) node).getOccurrenceMark()).append(">");
            }
        }
    }

    @Override
    public boolean branchCanProduceResults(List<PatternOccurrence> patternsWithOccurrences) {
        //test if there are all components of the subpattern in the branch
        final boolean keepBranch = nodeRegexes.stream()
                .allMatch(nodeRegex -> patternsWithOccurrences.stream().map(patternOccurrence -> patternOccurrence.pattern)
                        .anyMatch(pattern -> pattern.getEventNodes().stream()
                                .anyMatch(n -> nodeRegex.matcher(n.getStringEventId()).matches())));
        if (!keepBranch) {
            branchesCutCount++;
        }
        return keepBranch;
    }

    @Override
    public int getBranchesCutCount() {
        return branchesCutCount;
    }

    @Override
    public String toString() {
        return "Subpattern " + subPattern + " constraint";
    }
}
