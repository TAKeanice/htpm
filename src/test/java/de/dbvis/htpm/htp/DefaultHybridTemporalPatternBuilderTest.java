package de.dbvis.htpm.htp;

import de.dbvis.htpm.htp.eventnodes.EventNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DefaultHybridTemporalPatternBuilderTest {

    @Test
    public void buildSubPatternByDeletingNode() {
        var pattern = new DefaultHybridTemporalPattern("a<b+0=c<b-0<b+1<b-1");

        var subPatterns = new DefaultHybridTemporalPattern[] {
                new DefaultHybridTemporalPattern("b+0=c<b-0<b+1<b-1"),
                new DefaultHybridTemporalPattern("a<c<b+0<b-0"),
                new DefaultHybridTemporalPattern("a<b+0<b-0<b+1<b-1"),
                new DefaultHybridTemporalPattern("a<c<b+0<b-0"),
                new DefaultHybridTemporalPattern("a<b+0=c<b-0"),
                new DefaultHybridTemporalPattern("a<b+0=c<b-0")
        };

        final List<EventNode> nodes = pattern.getEventNodes();
        for (int i = 0; i < nodes.size(); i++) {
            assertEquals("pattern " + (i+1) + " was wrong",
                    subPatterns[i], DefaultHybridTemporalPatternBuilder.buildSubPatternByDeletingNode(pattern, i));
        }
    }
}
