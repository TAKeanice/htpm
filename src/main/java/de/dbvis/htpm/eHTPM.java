package de.dbvis.htpm;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Wolfgang Jentner <wolfgang.jentner@uni.kn> on 12/17/2015 10:55 AM.
 */
public class eHTPM extends HTPM {

    private int maxgap;

    /**
     * Creates a new HTPM-Algorithm-Object.
     *
     * @param d           - The Database containing the series.
     * @param min_support - The minimum support.
     */
    public eHTPM(HybridEventSequenceDatabase d, double min_support, int maxgap) {
        super(d, min_support);
        this.maxgap = maxgap;
    }

    public int getMaxGap() {
        return this.maxgap;
    }

    protected Map<HybridTemporalPattern, List<Occurrence>> join(final HybridTemporalPattern prefix, final HybridTemporalPattern p1, final List<Occurrence> or1, final HybridTemporalPattern p2, final List<Occurrence> or2, final int k) {
        final Map<HybridTemporalPattern, List<Occurrence>> map = new HashMap<>();

        for (final Occurrence s1 : or1) {
            for (final Occurrence s2 : or2) {
                if (!s1.getHybridEventSequence().getSequenceId().equals(s2.getHybridEventSequence().getSequenceId())) {
                    continue;
                }

                final Map<HybridTemporalPattern, Occurrence> m = ORAlign(prefix, p1, s1, p2, s2);

                m.entrySet()
                        .stream()
                        .filter(e -> e.getKey().length() == k)
                        .forEach(e -> {

                            final HybridTemporalPattern p = e.getKey();
                            final Occurrence o = e.getValue();

                            if (!map.containsKey(p)) {
                                map.put(p, new LinkedList<>());
                            }

                            if (!map.get(p).contains(o) && !this.isOverMaxGap(o)) {
                                map.get(p).add(o);
                            }
                        });
            }
        }

        return filterHybridTemporalPatterns(map);
    }

    protected boolean isOverMaxGap(Occurrence occurrence) {
        if(this.maxgap <= 0) {
            return false;
        }


        double dif;

        for(int i = 1; i < occurrence.size(); i++) {

            dif = occurrence.get(i).getTimePoint() - occurrence.get(i-1).getTimePoint();
            if(dif < 0) {
                throw new RuntimeException("dif was negtavie, we have to sort the list first");
            }
            if(dif > this.maxgap) {
                return true;
            }
        }

        return false;
    }

}
