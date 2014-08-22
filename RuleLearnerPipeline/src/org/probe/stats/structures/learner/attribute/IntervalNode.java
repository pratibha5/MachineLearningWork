/*
 * @(#)IntervalNode.java    1.1 2002/01/21
 */

package org.probe.stats.structures.learner.attribute;

import java.text.NumberFormat;
import java.util.StringTokenizer;

/**
 * A class representing a range <code>[begin, end)</code>. Used to deal with
 * continuous variables.
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization.
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 * 
 * @version 2004/11/22
 * @author Philip Ganchev Made the number format static; now all instances of
 *         IntervalNode share a single number format.
 */
public class IntervalNode extends VHierarchyNode implements Cloneable {
	public double begin;

	public double end;

	protected final static int DECIMAL_PLACES = 3;

	protected static NumberFormat nf;

	public IntervalNode(String attnm, double a, double b) {
		begin = a;
		end = b;
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(DECIMAL_PLACES);
		nf.setMinimumFractionDigits(DECIMAL_PLACES);
		name = nameString();
		attName = attnm;
	}

	public IntervalNode(String attnm, Double a, Double b) {
		this(attnm, a.doubleValue(), b.doubleValue());
	}

	/**
	 * Parses <code>name</code> for the interval start and end and assigns
	 * them to <code>begin</code> and <code>end</code>.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */
	public boolean setName(String name) {
		StringTokenizer tok = new StringTokenizer(name, ":");
		double a = 0, b = 0;
		boolean bReturn = false;

		if (tok.countTokens() > 1) {
			if (tok.hasMoreElements()) {
				a = (new Double(tok.nextElement().toString())).doubleValue();
			}

			if (tok.hasMoreElements()) {
				b = (new Double(tok.nextElement().toString())).doubleValue();
				bReturn = true;
			}
		}

		if (bReturn) {
			begin = a;
			end = b;
		}

		name = nameString();

		return bReturn;
	}

	private String nameString() {
		// return nf.format(begin) + " <= x < " + nf.format(end);
		return 
			(begin == Double.NEGATIVE_INFINITY ? "-inf" : nf.format(begin))
			+ ".."
			+ (end == Double.POSITIVE_INFINITY ? "inf" : nf.format(end));
	}

	/**
	 * Returns <code>true</code> if both ends of the interval are equal. This
	 * is an iffy condition as we are comparing doubles for equality.
	 */
	public boolean equals(IntervalNode n) {
		return (this == n) || (n.begin == begin && n.end == end);
	}

	/**
	 * Returns <code>true</code> if the double value is inside this interval;
	 * that is, <code>begin</code> <= d < <code>end</code>
	 */
	public boolean equals(Double d) {
		return (d.doubleValue() >= begin) && (d.doubleValue() < end);
	}

	/**
	 * If the object is a Double or an IntervalNode, the appropriate method is
	 * called. Otherwise, this method returns false.
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof Double)
			return equals((Double) o);
		if (o instanceof IntervalNode)
			return equals((IntervalNode) o);
		return false;
	}

	/**
	 * Value compare similar to the equals methods.
	 */
	public int compareTo(Object o) {
		// If 'o' is a double, the value can be placed in relation to the
		// interval
		if (o instanceof Double) {
			double dCompare = ((Double) o).doubleValue();
			if (dCompare >= end) {
				return -1;
			} else if (dCompare <= begin) {
				return 1;
			} else {
				return 0;
			}
		}
		// If 'o' is an interval (for some reason), then only compare for
		// equality.
		else if (o instanceof IntervalNode && equals((IntervalNode) o)) {
			return 0;
		}

		return VHierarchyNode.NO_COMPARE;
	}

	/**
	 * Returns a copy of this node that is not positioned within any tree
	 */
	public Object clone() {
		IntervalNode sn = new IntervalNode(this.attName, begin, end);
		sn.nodeCount = this.nodeCount;
		sn.nodeList = new VHierarchyNode[this.nodeList.length];
		for (int i = 0; i < this.nodeList.length; i++) {
			VHierarchyNode clnLstNd = (VHierarchyNode) this.nodeList[i].clone();
			sn.insert(clnLstNd, sn.getChildCount());
			sn.nodeList[i] = clnLstNd;
		}
		return sn;
	}
}
