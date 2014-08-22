/* @(#)AttributeComaprator.java */

package structures.learner.attribute;

import structures.learner.attribute.LearnerAttribute;

/**
 * AttributeComparator orders Attributes based on the natural order of their
 * indeces.
 * 
 * @version 2004-04-22
 * @author Philip Ganchev
 */

public class AttributeComparator implements java.util.Comparator {
	public int compare(Object o1, Object o2) {
		int i1 = ((LearnerAttribute) o1).getIndex();
		int i2 = ((LearnerAttribute) o2).getIndex();
		return ((i1 < i2) ? -1 : (i1 == i2) ? 0 : 1);
	}
}
