/*
 * @(#)GE.java    1.1 02/01/21
 *
 */

package org.probe.stats.structures.constraints;

import java.util.ArrayList;

/**
 * Less than Relationship (Number of objects with the left tag must be >= right)
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
public class GE extends Relationship {
	public GE() {
		super(">=");
		rightClass = Integer.class;
	}

	public boolean satisfied(Object left, Object right, ArrayList tags) {
		int nLeft = 0;
		// Count the numbers
		for (int x = 0; x < tags.size(); x++) {
			if (tags.get(x).equals(left))
				nLeft++;
		}

		int nRight = ((Integer) right).intValue();

		return nLeft >= nRight;
	}
}
