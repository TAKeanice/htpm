package de.dbvis.htpm;

import de.dbvis.htpm.db.HybridEventSequenceDatabase;
import de.dbvis.htpm.htp.HybridTemporalPattern;
import de.dbvis.htpm.occurrence.DefaultOccurrence;
import de.dbvis.htpm.occurrence.Occurrence;

import java.util.*;

/**
 * This is an extension of the HTPM Algorithm.
 * It introduces the max-gap constraint in order to reduce the result set.
 * Created by Wolfgang Jentner <wolfgang.jentner@uni.kn> on 12/17/2015 10:55 AM.
 */
public class eHTPM extends HTPM {

    private int maxgap;

    /**
     * Creates a new HTPM-Algorithm-Object.
     *
     * @param d           - The Database containing the series.
     * @param min_support - The minimum support.
     * @param maxgap      - The max gap constraint, if 0 maxgap will be ignored.
     */
    public eHTPM(HybridEventSequenceDatabase d, double min_support, int maxgap) {
        super(d, min_support);
        this.maxgap = maxgap;
    }

    public int getMaxGap() {
        return this.maxgap;
    }

    @Override
    protected Map<HybridTemporalPattern, List<Occurrence>> join(final HybridTemporalPattern prefix, final HybridTemporalPattern p1, final List<Occurrence> or1, final HybridTemporalPattern p2, final List<Occurrence> or2) {
        final Map<HybridTemporalPattern, List<Occurrence>> map = new HashMap<>();

        for (int i = 0; i < or1.size(); i++) {
            Occurrence s1 = or1.get(i);
            Occurrence occPref = ((DefaultOccurrence) s1).getPrefix();
            for (int i1 = 0; i1 < or2.size(); i1++) {
                Occurrence s2 = or2.get(i1);
                //make sure it is valid to merge the two occurrence records: only if they have same prefix (hence also from same sequence)
                //other rare case: when we perform self-join on pattern, both ORs could be the same - makes no sense to join (and in fact joins wrong)
                if (occPref != ((DefaultOccurrence) s2).getPrefix()
                        || or1 == or2) {
                    continue;
                }

                final Map<HybridTemporalPattern, Occurrence> m = ORAlign(prefix, p1, s1, p2, s2);

                m.forEach((p, o) -> {
                    if (!map.containsKey(p)) {
                        map.put(p, new ArrayList<>());
                    }

                    //TODO: this is the wrong place for a maxGap constraint.
                    // Gaps in patterns could be closed later, thus patterns that must be there for successive candidate generation are erraneously pruned.
                    // Instead, we could introduce a maxDuration constraint, which could be applied here.
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
                throw new RuntimeException("dif was negative, we have to sort the list first but it should be already sorted");
            }
            if(dif > this.maxgap) {
                return true;
            }
        }

        return false;
    }

}
