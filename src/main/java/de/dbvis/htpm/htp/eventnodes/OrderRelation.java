package de.dbvis.htpm.htp.eventnodes;

/**
 * There are only two different types of OrderRelations.
 * <br/> = indicates that two EventNodes occur at the same time
 * <br/> &lt; indicates that (e1 &lt; e2) e1 occurs before e2
 * @author Wolfgang Jentner
 *
 */
public enum OrderRelation implements HTPItem {

	SMALLER,
	EQUAL;

	public static OrderRelation fromChar(char c) {
		return c == '<' ? SMALLER : EQUAL;
	}

    public String toString() {
		if(this == OrderRelation.SMALLER) {
			return "<";
		} else {
			return "=";
		}
	}
}
