package de.dbvis.htpm.occurrence;

import de.dbvis.htpm.hes.events.HybridEvent;

public class DefaultOccurrencePoint implements OccurrencePoint {

    private final double timepoint;
    private final HybridEvent hybridEvent;

    public DefaultOccurrencePoint(HybridEvent hybridEvent) {
        this(hybridEvent, true);
    }

    public DefaultOccurrencePoint(HybridEvent hybridEvent, boolean isStartPoint) {
        this.timepoint = hybridEvent.isPointEvent() ? hybridEvent.getTimePoint()
                : (isStartPoint ? hybridEvent.getStartPoint() : hybridEvent.getEndPoint());
        this.hybridEvent = hybridEvent;
    }

    @Override
    public double getTimePoint() {
        return timepoint;
    }

    @Override
    public HybridEvent getHybridEvent() {
        return hybridEvent;
    }

    @Override
    public int hashCode() {
        return hybridEvent.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OccurrencePoint) {
            OccurrencePoint other = (OccurrencePoint) obj;
            return this.timepoint == other.getTimePoint()
                    && this.hybridEvent.equals(other.getHybridEvent());
        }
        return false;
    }

    @Override
    public int compareTo(OccurrencePoint o) {
        return Double.compare(timepoint, o.getTimePoint());
    }
}
