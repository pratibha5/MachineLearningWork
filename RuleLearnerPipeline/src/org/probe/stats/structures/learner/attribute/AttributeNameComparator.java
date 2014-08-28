/* @(#)AttributeNameComaprator.java */

package org.probe.stats.structures.learner.attribute;

import org.probe.stats.structures.learner.attribute.LearnerAttribute;

/**
 * AttributeComparator orders Attributes based on the alphabetical order of
 * their names.
 * 
 * @version 2005-01-02
 * @author Philip Ganchev
 */

public class AttributeNameComparator implements java.util.Comparator {
	public int compare(Object o1, Object o2) {
		String name1 = ((LearnerAttribute) o1).getName();
		String name2 = ((LearnerAttribute) o2).getName();
		return name1.compareTo(name2);
	}
}
