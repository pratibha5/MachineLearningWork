/*
 * @(#)OR.java    1.1 02/01/21
 *
 */

package structures.constraints;

import java.util.ArrayList;

/**
 * OR Relationship (Rule must contain A or B)
 * 
 * Requires that one of two tags must be in ALL rules
 * 
 * @version 1.0; 06/27/2000
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 02/01/21
 * @author Will Bridewell
 */

public class OR extends Relationship {
	public OR() {
		super("OR");
	}

	public boolean satisfied(Object left, Object right, ArrayList tags) {
		if (tags.contains(left) || tags.contains(right))
			return true;

		return false;
	}
}
