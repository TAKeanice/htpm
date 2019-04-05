package de.dbvis.htpm.constraints;

import de.dbvis.htpm.htp.DefaultHybridTemporalPattern;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.htp.eventnodes.*;

import java.util.List;

public class SubPatternConstraint extends RegularExpressionConstraint {

    public SubPatternConstraint(String subPattern) {
        super(toRegex(subPattern), false);
    }

    private static String toRegex(String subPattern) {
        //language=RegExp
        String allEqualsPlaceholder = "([^<]+)";
        String allEventsPlaceholder = ".*";

        HybridTemporalPattern p = new DefaultHybridTemporalPattern(subPattern);
        List<HTPItem> nodes = p.getPatternItemsInStringIdOrder();

        StringBuilder regex = new StringBuilder(allEventsPlaceholder);

        for (int i = 0; i < p.getOrderRelations().size(); i++) {
            appendNode(regex, (EventNode) nodes.get(i * 2));
            regex.append(p.getOrderRelations().get(i) == OrderRelation.EQUAL ? allEqualsPlaceholder : allEventsPlaceholder);
        }
        //add last node (and wildcard for everything that might come afterwards)
        appendNode(regex, (EventNode) nodes.get(nodes.size() - 1));
        regex.append(allEventsPlaceholder);

        return regex.toString();
    }

    private static void appendNode(StringBuilder regex, EventNode node) {
        final String stringEventId = node.getStringEventId();
        regex.append(stringEventId);
        if (node instanceof IntervalEventNode) {
            final String cleanedId = stringEventId.replaceAll("[^A-Za-z0-9]", "X");
            if (node instanceof IntervalStartEventNode) {
                //capture occurrencemark in regex with named reference
                regex.append("\\+" + "(?<").append(cleanedId).append(((IntervalEventNode) node).getOccurrenceMark()).append(">[0-9]*)");
            } else if (node instanceof IntervalEndEventNode) {
                //backreference to start node occurrence mark
                regex.append("\\+\\").append(cleanedId).append(((IntervalEventNode) node).getOccurrenceMark());
            }
        }
    }
}
