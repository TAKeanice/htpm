package de.dbvis.htpm.htp.eventnodes;

/**
 * There are only two different types of OrderRelations.
 * <br/> = indicates that two EventNodes occur at the same time
 * <br/> &lt; indicates that (e1 &lt; e2) e1 occurs before e2
 * @author Wolfgang Jentner
 *
 */
public final class OrderRelation implements HTPItem {
	/**
	 * &lt;
	 */
	public static final OrderRelation SMALLER = new OrderRelation();
	
	/**
	 * =
	 */
	public static final OrderRelation EQUAL = new OrderRelation();
	
	/**
	 * Do not allow any different objects than the two defined above
	 */
	private OrderRelation() {}

	public String toString() {
		if(this == OrderRelation.SMALLER) {
			return "<";
		} else {
			return "=";
		}
	}
}