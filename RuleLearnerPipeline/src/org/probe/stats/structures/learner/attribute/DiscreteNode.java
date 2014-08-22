/*
 * @(#)DiscreteNode.java    1.1 2002/01/21
 */

package org.probe.stats.structures.learner.attribute;

// Java.io

/**
 * Discrete node in the hierarchy of attribute values
 * 
 * @version 1.0 2000/03/20
 * @author Jeremy Ludwig
 * 
 * Edited to make use of new code organization
 * 
 * @version 1.1 2002/01/21
 * @author Will Bridewell
 */
public class DiscreteNode extends VHierarchyNode implements Cloneable {
	public DiscreteNode(String attNm, String name) {
		super(attNm, name);
	}

	public DiscreteNode(String attNm, double value) {
		super(attNm, new String("" + value));
	}

	/**
	 * String comparison
	 */
	public int compareTo(Object o) {
		if (o instanceof IntervalNode)
			return VHierarchyNode.NO_COMPARE;

		return name.compareTo(o.toString());
	}

	/**
	 * Returns a copy of this node and its subtree
	 */
	public Object clone() {
		DiscreteNode sn = new DiscreteNode(this.attName, this.name);
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
