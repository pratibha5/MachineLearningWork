/*
 * @(#)Conjunct.java    1.1 2002/11/03
 */

package org.probe.rule;


import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;
import java.util.Scanner;
import data.dataset.*;
import org.probe.stats.structures.learner.attribute.*;
//import structures.learner.rule.*;

/**
 * A list of Conjuncts, representing a conjunction of attribute-value pairs. 
 * 
 * @version 1.5 2008/1/28
 * @author Jonathan Lustgarten
 */

public class Conjunction extends ArrayList {

	public Conjunction() {
		super();
	}

	public Conjunction(int nConjuncts) {
		super(nConjuncts);
	}

	public Conjunction(Scanner scanner) throws IOException {
		Conjunct conj; 
		//do {
			conj = new Conjunct(scanner);
			conj = new Conjunct(scanner);
			//System.out.println(conj);
			if (conj != null) {
				add(conj);
			}
		//} while (conj != null);
	}
	
	public Conjunct getConjunct(int i) {
		return (Conjunct) get(i);
	}

	public void addConjunct(Conjunct c) {
		add(c);
	}

	public String toString() {
		StringBuffer b = new StringBuffer("(");
		for (int i = 0; i < size() - 1; i++) {
			Conjunct c = (Conjunct) get(i);
			// if (!c.getAttribute().isIgnore()) {
			b.append(get(i) + " ");
			// }
		}
		if (size() > 0) {
			b.append(get(size() - 1));
		}
		return b.append(")").toString();
	}

	public void replaceConjunct(Conjunct old, Conjunct newC) {
		int i = indexOf(old);
		if (i > -1) {
			set(i, newC);
		}
	}

	/**
	 * @param d
	 *            the datum to be matched
	 * @return <code>false</code> if one of the attribute-value pairs of this
	 *         conjunction does not match <code>d</code> and its attribute is
	 *         not ignored, <code>true</code> otherwise.
	 */
	public boolean matchesDatum(DataModel d, int instIx) {
		for (Iterator i = iterator(); i.hasNext();) {
			Conjunct c = (Conjunct) i.next();
			if (!c.matchesDatum(d, instIx)) // && !c.getAttribute().isIgnore())
				return false;
		}
		return true;
	}

	public boolean matches(LearnerAttribute a) {
		for (Iterator i = iterator(); i.hasNext();) {
			Conjunct conjunct = (Conjunct) i.next();
			if (conjunct.matches(a)) {
				return true;
			}
		}
		return false;
	}

	// We can't use AttributeList unless we have the program state.
	public ArrayList<String> getAttributes() {
		ArrayList<String> atts = new ArrayList<String>(size());
		for (Iterator i = iterator(); i.hasNext();) {
			Conjunct c = (Conjunct) i.next();
			atts.add(c.getAttributeName());
		}
		atts.trimToSize();
		return atts;
	}

	public Object clone() {
		return super.clone();
	}
}
