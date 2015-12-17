package de.dbvis.htpm.htp.eventnodes;

import de.dbvis.htpm.hes.events.HybridEvent;
import de.dbvis.htpm.occurrence.OccurrencePoint;

/**
 * This abstracts class implements a HTPItem as well as an OccurrencePoint, it is also
 * comparable.
 * An EventNode is the basic, abstract object and must be defined further, either as
 * a PointEventNode or an IntervalEventNode.
 * @author Wolfgang Jentner
 *
 */
public abstract class EventNode implements HTPItem, OccurrencePoint, Comparable<EventNode> {
	/**
	 * The id of the EventNode, if available this should match the id of the HybridEvent
	 */
	protected String id;
	
	/**
	 * The time point of the EventNode, if available this should match the time point of the HybridEvent
	 */
	protected double timepoint;
	
	/**
	 * The associated HybridEvent, may be null
	 */
	protected HybridEvent hybridevent;

	private boolean pointEvent;

	private boolean endEvent;
	
	/**
	 * This constructor is called by all extending classes,
	 * it creates an EventNode based on the information the HybridEvent
	 * is providing. The extending classes define by their instance what type
	 * of information is necessary, this indicated by the boolean flags.
	 * @param hybridevent the HybridEvent, may not be null
	 * @param ispointevent true if this constructor is called by the PointEventNode, false otherwise
	 * @param isendevent true if this constructor is called by the IntervalEndEventNode, false otherwise
	 */
	protected EventNode(HybridEvent hybridevent, boolean ispointevent, boolean isendevent) {
		if(hybridevent == null) {
			throw new NullPointerException("HybridEvent must not be null");
		}
		if(ispointevent && !hybridevent.isPointEvent()) {
			throw new IllegalArgumentException("HybridEvent must be of type point event");
		}
		if(!ispointevent && hybridevent.isPointEvent()) {
			throw new IllegalArgumentException("HybridEvent must be of type interval event");
		}
		
		this.hybridevent = hybridevent;
		this.checkAndSetEventNodeId(hybridevent.getEventId());
		if(ispointevent && hybridevent.isPointEvent()) {
			this.timepoint = hybridevent.getTimePoint();
			this.pointEvent = true;
			this.endEvent = false;
		} else if(!ispointevent && !isendevent && !hybridevent.isPointEvent()) {
			this.timepoint = hybridevent.getStartPoint();
			this.pointEvent = false;
			this.endEvent = false;
		} else if(!ispointevent && isendevent && !hybridevent.isPointEvent()) {
			this.timepoint = hybridevent.getEndPoint();
			this.pointEvent = false;
			this.endEvent = true;
		} else {
			throw new RuntimeException("whoops, something went wrong here, this should not happen");
		}
	}
	
	/**
	 * Creates a new EventNode without any association to a HybridEvent.
	 * @param id the id of the EventNode
	 * @param timepoint the time point of the EventNode
	 */
	protected EventNode(String id, double timepoint, boolean pointEvent, boolean endEvent) {
		this.checkAndSetEventNodeId(id);
		this.timepoint = timepoint;
		this.pointEvent = pointEvent;
		this.endEvent = endEvent;
	}
	
	/**
	 * Returns the id of the EventNode
	 * @return the id of the EventNode
	 */
	public String getEventNodeId() {
		return this.id;
	}
	
	/**
	 * Returns the time point of the EventNode.
	 * @return the time point of the EventNode
	 */
	public double getTimePoint() {
		return this.timepoint;
	}
	
	/**
	 * Returns the HybridEvent the EventNode is associated with.
	 * @return the HybridEvent; may be null
	 */
	public HybridEvent getHybridEvent() {
		return this.hybridevent;
	}
	

	/**
	 * This comparison is defined in the paper in definition 6.
	 */
	@Override
	public int compareTo(final EventNode o) {
		//As Definition 6
		
		//1 time
		if(this.getTimePoint() != o.getTimePoint()) {
			if(this.getTimePoint() > o.getTimePoint()) {
				return 1;
			} else if(this.getTimePoint() < o.getTimePoint()) {
				return -1;
			}
			//return (int) (this.getTimePoint() - o.getTimePoint());
		}
		
		//2 ID - alphabetically
		if(!this.getEventNodeId().equals(o.getEventNodeId())) {
			return this.getEventNodeId().compareTo(o.getEventNodeId());
		}
		
		//3 types
		//a
		if(this instanceof IntervalStartEventNode && o instanceof IntervalEndEventNode) {
			return -1;
		}
		//a-reverse
		if(o instanceof IntervalStartEventNode && this instanceof IntervalEndEventNode) {
			return 1;
		}
		
		//b
		if(this instanceof IntervalStartEventNode && o instanceof PointEventNode) {
			return -1;
		}
		//b-reverse
		if(o instanceof IntervalStartEventNode && this instanceof PointEventNode) {
			return 1;
		}
		
		//c
		if(this instanceof PointEventNode && o instanceof IntervalEndEventNode) {
			return -1;
		}
		//c-reverse
		if(o instanceof PointEventNode && this instanceof IntervalEndEventNode) {
			return 1;
		}
		
		//4 occurencemark
		if(this instanceof IntervalEventNode && o instanceof IntervalEventNode) {
			return ((IntervalEventNode) this).getOccurrenceMark() - ((IntervalEventNode) o).getOccurrenceMark();
		}
		
		//tie
		return 0;
	}
	
	/**
	 * This abstract method must be implemented by the extending classes,
	 * the representation varies by the type of EventNode.
	 * @return A representation of the EventNode
	 */
	public abstract String toString();
	
	/**
	 * This abstract method must be implemented by the extending classes,
	 * the equality is defined by each individual type.
	 * @param o the EventNode to check against
	 * @return true if the EventNodes are equal, false otherwise
	 */
	public abstract boolean equals(Object o);
	
	protected void checkAndSetEventNodeId(String id) {
		if(id == null) {
			throw new NullPointerException("EventNodeId must not be null");
		}
		if(id.matches("[\\+\\-<=]")) {
			throw new IllegalArgumentException("EventNodeId must not contain <,=,+,-");
		}
		this.id = id;
	}

	public boolean isPointEvent() {
		return pointEvent;
	}

	public boolean isEndEvent() {
		return endEvent;
	}

	public boolean isStartEvent() {
		return !pointEvent && !endEvent;
	}
}
