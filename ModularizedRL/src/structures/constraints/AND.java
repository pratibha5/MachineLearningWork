/*
 * @(#)AND.java    1.1 02/01/21
 *
 */

package structures.constraints;

import java.util.ArrayList;

/**
 * AND Relationship (Rule must contain A and B)
 * 
 * Requires that BOTH tags must be in ALL rules - not very interesting - can be
 * duplicated by a >= 1 and b >= 1
 * 
 * @version 1.0; 06/27/2000
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 02/01/21
 * @author Will Bridewell
 */

public class AND extends Relationship {
	public AND() {
		super("AND");
	}

	public boolean satisfied(Object left, Object right, ArrayList tags) {
		if (tags.contains(left) && tags.contains(right))
			return true;

		return false;
	}
}
