/*
 * @(#)XOR.java    1.1 02/01/21
 *
 */

package structures.constraints;

import java.util.ArrayList;

/**
 * XOR Relationship (Rule must contain A or B)
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

public class XOR extends Relationship {
	public XOR() {
		super("XOR");
	}

	public boolean satisfied(Object left, Object right, ArrayList tags) {
		boolean bLeft = tags.contains(left);
		boolean bRight = tags.contains(right);

		if ((bLeft || bRight) && !(bLeft && bRight))
			return true;

		return false;
	}

	public boolean unsatisfiable(Object left, Object right, ArrayList tags) {
		boolean bLeft = tags.contains(left);
		boolean bRight = tags.contains(right);

		return (bLeft && bRight);
	}
}
