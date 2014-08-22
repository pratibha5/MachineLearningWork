/*
 * @(#)Constraint.java    1.1 02/01/21
 */

package org.probe.stats.structures.constraints;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;

import org.probe.rule.Conjunct;
import org.probe.rule.Rule;

/**
 * This class represents constraint objects (e.g. number of labs is <= 2) that
 * will be applied to rules to determine if they meet the minimum
 * qualifications.
 * 
 * At this point, all constraints contain a left hand side, right hand side, and
 * a relationship between the two. Constraints cannot be nested.
 * 
 * @version 1.0 00/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 02/01/21
 * @author Will Bridewell
 */

public class Constraint implements Serializable {
	/**
	 * The object to the left
	 */
	Object left;

	/**
	 * The object to the right
	 */
	Object right;

	/**
	 * The relationship between the left and right
	 * 
	 * @see Relationship
	 */
	Relationship rel;

	/**
	 * Default constructor
	 */
	public Constraint(Object left, Object right, Relationship r) {
		this.left = left;
		this.right = right;
		this.rel = r;
	}

	/**
	 * Returns a string representation of the constraint
	 */
	public String toString() {
		return "( " + left + " " + rel + " " + right + " )";
	}

	/**
	 * Returns true if r satisfies all of the applicable constraints.
	 */
	public boolean goodRule(Rule r) {
		// Compile the rules tags into a list
		ArrayList list = new ArrayList();
		Enumeration en = r.elements();
		while (en.hasMoreElements()) {
			Conjunct c = (Conjunct) en.nextElement();
			// list.addAll(c.getAttribute().getTagList());
		}

		return rel.satisfied(left, right, list);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(left);
		out.writeObject(right);
		out.writeObject(rel);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		left = new Object();
		left = in.readObject();
		right = new Object();
		right = in.readObject();
		rel = new Relationship();
		rel = (Relationship) in.readObject();
	}
}
