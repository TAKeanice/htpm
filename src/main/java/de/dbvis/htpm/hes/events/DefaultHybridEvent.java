package de.dbvis.htpm.hes.events;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class implements a HybridEvent.
 * 
 * @author Wolfgang Jentner
 *
 */
public class DefaultHybridEvent implements HybridEvent {
	/**
	 * The id of the HybridEvent, may not contain <,=,+,-
	 */
	protected String id;
	
	/**
	 * The start point of the HybridEvent, is also equal to the time point if the
	 * HybridEvent is of type point based event
	 */
	protected double startpoint;
	
	/**
	 * The end point of the HybridEvent, null if HybridEvent is of type interval based event.
	 */
	protected Double endpoint;
	
	/**
	 * This constructor only initiates a HybridEvent without a timepoint.
	 * 
	 * @param id The id of the HybridEvent.
	 */
	private DefaultHybridEvent(String id) {
		if(id == null) {
			throw new NullPointerException("ID must not be null");
		}
		if(id.matches("[\\+\\-<=]")) {
			throw new IllegalArgumentException("The Id may not contain (, ), <, =, +, - (wrong id: " + id + ")");
		}
		this.id = id;
	}
	
	/**
	 * This constructor initiates a point based HybridEvent.
	 * @param id The id of the HybridEvent
	 * @param timepoint The time point where the HybridEvent occurs
	 */
	public DefaultHybridEvent(String id, double timepoint) {
		this(id);
		this.startpoint = timepoint;
	}
	
	/**
	 * Creates a new interval based HybridEvent.
	 * @param id The id of the HybridEvent
	 * @param startpoint The time point when the HybridEvent starts
	 * @param endpoint The time point when the HybridEvent ends
	 */
	public DefaultHybridEvent(String id, double startpoint, double endpoint) {
		this(id, startpoint, (Double) endpoint);
	}

	public DefaultHybridEvent(String id, double startpoint, Double endpoint) {
		this(id);
		if(endpoint != null && endpoint <= startpoint) {
			throw new IllegalArgumentException("endpoint cannot occur before or at the same time as the startpoint");
		}
		this.startpoint = startpoint;
		this.endpoint = endpoint;
	}
	
	@Override
	public String getEventId() {
		return id;
	}

	public String toString() {
		String s2 = (this.endpoint != null) ? ","+this.endpoint : "";
		return "("+id+",("+this.startpoint+s2+"))";
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof HybridEvent)) {
			return false;
		}
		if(o == this) {
			return true;
		}

		HybridEvent o1 = (HybridEvent) o;
		return new EqualsBuilder()
				.append(this.getEventId(), o1.getEventId())
				.append(this.isPointEvent(), o1.isPointEvent())
				.append(this.getStartPoint(), o1.getStartPoint())
				.append(this.getEndPoint(), o1.getEndPoint())
				.isEquals();

//
//
//		if(o != null && o instanceof HybridEvent) {
//			HybridEvent e = (HybridEvent) o;
//			return e.getEventId().equals(this.getEventId())
//					&& e.getStartPoint() == this.getStartPoint()
//					&& e.isPointEvent() == this.isPointEvent()
//					&& ((e.getEndPoint() != null
//						&& e.getEndPoint().equals(this.getEndPoint()
//						)
//						|| (e.getEndPoint() == null && this.getEndPoint() == null)
//						)
//				);
//		}
//		return false;
	}

	public int hashCode() {
		return new HashCodeBuilder(7,13)
				.append(this.id)
				.append(this.startpoint)
				.append(this.endpoint)
				.toHashCode();
	}

	@Override
	public double getStartPoint() {
		return this.startpoint;
	}

	@Override
	public Double getEndPoint() {
		return this.endpoint;
	}

	@Override
	public double getTimePoint() {
		return this.startpoint;
	}

	@Override
	public boolean isPointEvent() {
		return this.endpoint == null;
	}
}
